package tech.agrowerk.business.service.barter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.barter.RegisterPartialDeliveryRequest;
import tech.agrowerk.application.dto.response.barter.CropCommitmentResponse;
import tech.agrowerk.application.dto.response.barter.PartialDeliveryResponse;
import tech.agrowerk.business.mapper.barter.BarterDeliveryMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.OperationDeniedException;
import tech.agrowerk.infrastructure.model.barter.*;
import tech.agrowerk.infrastructure.model.barter.enums.*;
import tech.agrowerk.infrastructure.repository.barter.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BarterDeliveryService {

    private final CropCommitmentRepository    commitmentRepository;
    private final PartialDeliveryRepository   deliveryRepository;
    private final BarterTransactionRepository transactionRepository;
    private final BarterContractRepository    contractRepository;
    private final BarterDeliveryMapper        mapper;
    private final AuthUtil                    authUtil;

    public BarterDeliveryService(CropCommitmentRepository commitmentRepository,
                                 PartialDeliveryRepository deliveryRepository,
                                 BarterTransactionRepository transactionRepository,
                                 BarterContractRepository contractRepository,
                                 BarterDeliveryMapper mapper,
                                 AuthUtil authUtil) {
        this.commitmentRepository  = commitmentRepository;
        this.deliveryRepository    = deliveryRepository;
        this.transactionRepository = transactionRepository;
        this.contractRepository    = contractRepository;
        this.mapper                = mapper;
        this.authUtil              = authUtil;
    }


    @Transactional
    public PartialDeliveryResponse registerDelivery(RegisterPartialDeliveryRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        CropCommitment commitment = commitmentRepository.findById(request.commitmentId())
                .orElseThrow(() -> new EntityNotFoundException("Commitment not found"));

        if (!commitment.getFarmer().getId().equals(auth.id()))
            throw new AccessDeniedException("Only the responsible farmer can register delivery");

        if (commitment.getStatus() == CommitmentStatus.DELIVERED
                || commitment.getStatus() == CommitmentStatus.CANCELLED)
            throw new OperationDeniedException("Commitment is already " + commitment.getStatus());

        BigDecimal alreadyDelivered = deliveryRepository
                .sumDeliveredByCommitment(commitment.getId());

        BigDecimal newTotal = alreadyDelivered.add(request.deliveredQuantity());

        if (newTotal.compareTo(commitment.getCommittedQuantity()) > 0)
            throw new OperationDeniedException(
                    "Delivered quantity exceeds committed. Committed: "
                            + commitment.getCommittedQuantity()
                            + ", Already delivered: " + alreadyDelivered
                            + ", Trying to add: " + request.deliveredQuantity()
            );

        PartialDelivery delivery = PartialDelivery.builder()
                .commitment(commitment)
                .deliveredQuantity(request.deliveredQuantity())
                .deliveryDate(request.deliveryDate())
                .moisturePercentage(request.moisturePercentage())
                .impurityPercentage(request.impurityPercentage())
                .qualityGrade(request.qualityGrade())
                .notes(request.notes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        deliveryRepository.save(delivery);

        commitment.setDeliveredQuantity(newTotal);
        commitment.setUpdatedAt(LocalDateTime.now());

        if (newTotal.compareTo(commitment.getCommittedQuantity()) == 0) {
            commitment.setStatus(CommitmentStatus.DELIVERED);
            commitment.setActualDeliveryDate(request.deliveryDate());
            log.info("Commitment fully delivered id={}", commitment.getId());
            checkAndCompleteTransaction(commitment.getTransaction().getId());
        } else {
            commitment.setStatus(CommitmentStatus.PARTIALLY_DELIVERED);
        }

        log.info("Partial delivery registered commitment={} qty={}",
                commitment.getId(), request.deliveredQuantity());
        return mapper.toDeliveryResponse(delivery);
    }


    @Transactional(readOnly = true)
    public List<CropCommitmentResponse> listCommitmentsByTransaction(UUID transactionId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterTransaction t = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        boolean isParticipant = t.getOfferor().getId().equals(auth.id())
                || t.getAcceptor().getId().equals(auth.id());

        if (!isParticipant)
            throw new AccessDeniedException("Access denied");

        return commitmentRepository.findByTransaction_Id(transactionId)
                .stream().map(mapper::toCommitmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CropCommitmentResponse> listMyCommitments() {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        return commitmentRepository.findByFarmer_Id(auth.id())
                .stream().map(mapper::toCommitmentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PartialDeliveryResponse> listDeliveriesByCommitment(UUID commitmentId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        CropCommitment commitment = commitmentRepository.findById(commitmentId)
                .orElseThrow(() -> new EntityNotFoundException("Commitment not found"));

        boolean isParticipant =
                commitment.getFarmer().getId().equals(auth.id()) ||
                        commitment.getTransaction().getOfferor().getId().equals(auth.id()) ||
                        commitment.getTransaction().getAcceptor().getId().equals(auth.id());

        if (!isParticipant)
            throw new AccessDeniedException("Access denied");

        return deliveryRepository.findByCommitment_IdOrderByDeliveryDateDesc(commitmentId)
                .stream().map(mapper::toDeliveryResponse).toList();
    }


    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void markOverdueCommitments() {
        List<CropCommitment> overdue = commitmentRepository
                .findByExpectedDeliveryDateBeforeAndStatusNotIn(
                        LocalDate.now(),
                        List.of(CommitmentStatus.DELIVERED, CommitmentStatus.CANCELLED)
                );

        overdue.forEach(c -> {
            c.setStatus(CommitmentStatus.OVERDUE);
            c.setUpdatedAt(LocalDateTime.now());
        });

        if (!overdue.isEmpty())
            log.info("Marked {} commitments as OVERDUE", overdue.size());
    }


    private void checkAndCompleteTransaction(UUID transactionId) {
        boolean allDone = !commitmentRepository.existsByTransaction_IdAndStatusNotIn(
                transactionId,
                List.of(CommitmentStatus.DELIVERED, CommitmentStatus.CANCELLED)
        );

        if (!allDone) return;

        transactionRepository.findById(transactionId).ifPresent(t -> {
            t.setStatus(TransactionStatus.COMPLETED);
            t.setUpdatedAt(LocalDateTime.now());
            t.getBarterOffer().setStatus(OfferStatus.COMPLETED);
            log.info("Transaction completed id={}", transactionId);
        });

        contractRepository.findByTransaction_Id(transactionId).ifPresent(c -> {
            c.setContractStatus(ContractStatus.COMPLETED);
            c.setUpdatedAt(Instant.now());
            log.info("Contract completed for transaction={}", transactionId);
        });
    }
}
