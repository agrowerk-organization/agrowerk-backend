package tech.agrowerk.business.service.barter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.barter.AcceptTransactionRequest;
import tech.agrowerk.application.dto.request.barter.ProposeTransactionRequest;
import tech.agrowerk.application.dto.request.barter.SignContractRequest;
import tech.agrowerk.application.dto.response.barter.BarterContractResponse;
import tech.agrowerk.application.dto.response.barter.BarterTransactionResponse;
import tech.agrowerk.business.mapper.barter.BarterTransactionMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.OperationDeniedException;
import tech.agrowerk.infrastructure.model.barter.*;
import tech.agrowerk.infrastructure.model.barter.enums.CommitmentStatus;
import tech.agrowerk.infrastructure.model.barter.enums.ContractStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.TransactionStatus;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.repository.barter.*;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BarterTransactionService {

    private final BarterTransactionRepository barterTransactionRepository;
    private final BarterOfferRepository barterOfferRepository;
    private final BarterContractRepository barterContractRepository;
    private final BarterTransactionItemRepository barterTransactionItemRepository;
    private final BarterOfferItemRepository barterOfferItemRepository;
    private final CropCommitmentRepository cropCommitmentRepository;
    private final UserRepository userRepository;
    private final CropRepository cropRepository;
    private final BarterPricingService barterPricingService;
    private final BarterContractPdfService barterContractPdfService;
    private final BarterContractEmailService barterContractEmailService;
    private final BarterTransactionMapper barterTransactionMapper;
    private final AuthUtil authUtil;


    public BarterTransactionService(BarterTransactionRepository barterTransactionRepository,
                                    BarterOfferRepository barterOfferRepository,
                                    BarterContractRepository barterContractRepository,
                                    BarterTransactionItemRepository barterTransactionItemRepository,
                                    BarterOfferItemRepository barterOfferItemRepository,
                                    CropCommitmentRepository cropCommitmentRepository,
                                    UserRepository userRepository,
                                    CropRepository cropRepository,
                                    BarterPricingService barterPricingService,
                                    BarterContractPdfService barterContractPdfService,
                                    BarterContractEmailService barterContractEmailService,
                                    BarterTransactionMapper barterTransactionMapper,
                                    AuthUtil authUtil) {
        this.barterTransactionRepository = barterTransactionRepository;
        this.barterOfferRepository = barterOfferRepository;
        this.barterContractRepository = barterContractRepository;
        this.barterTransactionItemRepository = barterTransactionItemRepository;
        this.barterOfferItemRepository = barterOfferItemRepository;
        this.cropCommitmentRepository = cropCommitmentRepository;
        this.userRepository = userRepository;
        this.cropRepository = cropRepository;
        this.barterPricingService = barterPricingService;
        this.barterContractPdfService = barterContractPdfService;
        this.barterContractEmailService = barterContractEmailService;
        this.barterTransactionMapper = barterTransactionMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public BarterTransactionResponse proposeTransaction(ProposeTransactionRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        // ← lock aqui, na oferta
        BarterOffer offer = barterOfferRepository.findByIdWithLock(request.offerId())
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));

        if (offer.getStatus() != OfferStatus.ACTIVE)
            throw new OperationDeniedException("Offer is not active");

        if (offer.getOwner().getId().equals(auth.id()))
            throw new OperationDeniedException("You cannot propose on your own offer");

        boolean alreadyProposed = barterTransactionRepository
                .existsByBarterOffer_IdAndOfferor_IdAndStatusIn(
                        offer.getId(), auth.id(),
                        List.of(TransactionStatus.PENDING, TransactionStatus.CONFIRMED));

        if (alreadyProposed)
            throw new OperationDeniedException("You already have an active proposal for this offer");

        User offeror = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        BarterTransaction transaction = BarterTransaction.builder()
                .barterOffer(offer)
                .offeror(offeror)
                .acceptor(offer.getOwner())
                .offerorGives(request.offerorGives())
                .offerorCropQuantity(request.offerorCropQuantity())
                .offerorAssetQuantity(request.offerorAssetQuantity())
                .acceptorGives(offer.getOfferType())
                .acceptorCropQuantity(offer.getOfferedCropQuantity())
                .acceptorAsset(offer.getOfferedAsset())
                .offerorDeliveryDate(request.offerorDeliveryDate())
                .acceptorDeliveryDate(request.acceptorDeliveryDate())
                .status(TransactionStatus.PENDING)
                .notes(request.notes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        if (request.offerorCropId() != null) {
            Crop crop = cropRepository.findById(request.offerorCropId())
                    .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
            transaction.setOfferorCrop(crop);
        }

        BarterTransaction saved = barterTransactionRepository.save(transaction);
        log.info("Transaction proposed id={} offer={} by={}", saved.getId(), offer.getId(), auth.id());
        return barterTransactionMapper.toResponse(saved);
    }

    @Transactional
    public BarterTransactionResponse acceptTransaction(UUID transactionId,
                                                       AcceptTransactionRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterTransaction transaction = barterTransactionRepository.findByIdWithLock(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING)
            throw new OperationDeniedException("Only PENDING transactions can be accepted");

        if (transaction.getBarterOffer().getStatus() != OfferStatus.ACTIVE)
            throw new OperationDeniedException("The offer has already been accepted by someone else");

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.getBarterOffer().setStatus(OfferStatus.ACCEPTED);

        barterTransactionRepository.cancelAllPendingExcept(
                transaction.getBarterOffer().getId(), transactionId);

        List<BarterTransactionItem> items = copyOfferItems(transaction);

        BigDecimal totalValue = items.isEmpty()
                ? transaction.getBarterOffer().getRequestedValue() : null;

        BarterPriceSnapshot snapshot = barterPricingService.captureAndPersist(
                transaction,
                items,
                totalValue,
                request.commodity(),
                request.basisUsd()
        );

        generateCommitments(transaction, snapshot.getTotalBagsDue());

        generateContract(transaction, snapshot, items);

        log.info("Transaction accepted id={} bags={}", transactionId, snapshot.getTotalBagsDue());
        return barterTransactionMapper.toResponse(transaction);
    }
    @Transactional
    public void declineTransaction(UUID transactionId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        BarterTransaction t = findTransactionAndValidateAcceptor(transactionId, auth.id());

        if (t.getStatus() != TransactionStatus.PENDING)
            throw new OperationDeniedException("Only PENDING transactions can be declined");

        t.setStatus(TransactionStatus.CANCELLED);
        t.setUpdatedAt(LocalDateTime.now());
        log.info("Transaction declined id={}", transactionId);
    }


    @Transactional
    public void cancelTransaction(UUID transactionId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterTransaction t = barterTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        boolean isParticipant = t.getOfferor().getId().equals(auth.id())
                || t.getAcceptor().getId().equals(auth.id());


        if (!isParticipant)
            throw new AccessDeniedException("Only transaction participants can cancel it");

        if (t.getStatus() == TransactionStatus.COMPLETED)
            throw new OperationDeniedException("Completed transactions cannot be cancelled");

        if (t.getStatus() == TransactionStatus.CANCELLED)
            throw new OperationDeniedException("Transaction is already cancelled");

        TransactionStatus previousStatus = t.getStatus();
        t.setStatus(TransactionStatus.CANCELLED);
        t.setUpdatedAt(LocalDateTime.now());

        if (previousStatus == TransactionStatus.CONFIRMED) {
            t.getBarterOffer().setStatus(OfferStatus.ACTIVE);
        }

        log.info("Transaction cancelled id={}", transactionId);
    }

    @Transactional
    public BarterContractResponse signContract(SignContractRequest request,
                                               HttpServletRequest httpRequest) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        String clientIp = extractIp(httpRequest);

        BarterContract contract = barterContractRepository.findById(request.contractId())
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        BarterTransaction transaction = contract.getTransaction();
        boolean isOfferor  = transaction.getOfferor().getId().equals(auth.id());
        boolean isAcceptor = transaction.getAcceptor().getId().equals(auth.id());

        if (!isOfferor && !isAcceptor)
            throw new AccessDeniedException("Only transaction participants can sign the contract");

        if (isOfferor) {
            if (contract.getOfferorSignedAt() != null)
                throw new OperationDeniedException("You have already signed this contract");

            contract.setOfferorSignedAt(Instant.now());
            contract.setOfferorSignIp(clientIp);
            contract.setContractStatus(ContractStatus.AWAITING_ACCEPTOR_SIGNATURE);
            log.info("Contract signed by offeror id={} ip={}", contract.getId(), clientIp);
        }

        if (isAcceptor) {
            if (contract.getOfferorSignedAt() == null)
                throw new OperationDeniedException("Offeror must sign first");

            if (contract.getAcceptorSignedAt() != null)
                throw new OperationDeniedException("You have already signed this contract");

            contract.setAcceptorSignedAt(Instant.now());
            contract.setAcceptorSignIp(clientIp);
            contract.setContractStatus(ContractStatus.ACTIVE);

            transaction.setStatus(TransactionStatus.IN_PROGRESS);
            transaction.setUpdatedAt(LocalDateTime.now());
            log.info("Contract fully signed and ACTIVE id={} ip={}", contract.getId(), clientIp);
        }

        contract.setUpdatedAt(Instant.now());
        return barterTransactionMapper.toContractResponse(contract);
    }

    @Transactional(readOnly = true)
    public Page<BarterTransactionResponse> listMyTransactions(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        return barterTransactionRepository.findAllByUserId(auth.id(), pageable)
                .map(barterTransactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BarterTransactionResponse findById(UUID id) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterTransaction t = barterTransactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        boolean isParticipant = t.getOfferor().getId().equals(auth.id())
                || t.getAcceptor().getId().equals(auth.id());

        if (!isParticipant)
            throw new AccessDeniedException("Access denied");

        return barterTransactionMapper.toResponse(t);
    }

    @Transactional(readOnly = true)
    public BarterContractResponse findContract(UUID transactionId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterTransaction t = barterTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        boolean isParticipant = t.getOfferor().getId().equals(auth.id())
                || t.getAcceptor().getId().equals(auth.id());

        if (!isParticipant)
            throw new AccessDeniedException("Access denied");

        BarterContract contract = barterContractRepository.findByTransaction_Id(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Contract not found"));

        return barterTransactionMapper.toContractResponse(contract);
    }


    private BarterTransaction findTransactionAndValidateAcceptor(UUID transactionId, UUID userId) {
        BarterTransaction t = barterTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (!t.getAcceptor().getId().equals(userId))
            throw new AccessDeniedException("Only the acceptor can perform this action");

        return t;
    }

    private List<BarterTransactionItem> copyOfferItems(BarterTransaction transaction) {
        List<BarterOfferItem> offerItems = barterOfferItemRepository
                .findByBarterOffer_Id(transaction.getBarterOffer().getId());

        if (offerItems.isEmpty()) {
            return List.of();
        }

        List<BarterTransactionItem> items = offerItems.stream()
                .map(item -> BarterTransactionItem.builder()
                        .barterTransaction(transaction)
                        .input(item.getInput())
                        .quantity(item.getQuantity())
                        .unitOfMeasure(item.getUnitOfMeasure())
                        .unitPriceBrl(item.getUnitPriceBrl())
                        .totalPriceBrl(item.getTotalPriceBrl())
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        return barterTransactionItemRepository.saveAll(items);
    }

    private void generateCommitments(BarterTransaction t, BigDecimal totalBagsDue) {
        if (t.getOfferorCrop() != null) {
            cropCommitmentRepository.save(CropCommitment.builder()
                    .transaction(t)
                    .farmer(t.getOfferor())
                    .crop(t.getOfferorCrop())
                    .committedQuantity(totalBagsDue)
                    .deliveredQuantity(BigDecimal.ZERO)
                    .expectedDeliveryDate(t.getOfferorDeliveryDate())
                    .status(CommitmentStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        log.info("Commitments generated bags={} transaction={}", totalBagsDue, t.getId());
    }

    private void generateContract(BarterTransaction t,
                                  BarterPriceSnapshot snapshot,
                                  List<BarterTransactionItem> items) {
        String contractNumber = "BC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        LocalDate endDate = t.getAcceptorDeliveryDate().isAfter(t.getOfferorDeliveryDate())
                ? t.getAcceptorDeliveryDate() : t.getOfferorDeliveryDate();

        BarterContract contract = BarterContract.builder()
                .transaction(t)
                .contractNumber(contractNumber)
                .startDate(LocalDate.now())
                .endDate(endDate)
                .contractStatus(ContractStatus.AWAITING_OFFEROR_SIGNATURE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        barterContractRepository.save(contract);

        byte[] pdf = barterContractPdfService.generate(contract, snapshot, items);
        barterContractEmailService.sendContractToParties(contract, pdf);

        log.info("Contract generated + PDF send contract={}", contractNumber);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
