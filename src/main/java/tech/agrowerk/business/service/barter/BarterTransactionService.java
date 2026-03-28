package tech.agrowerk.business.service.barter;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import tech.agrowerk.infrastructure.model.barter.BarterContract;
import tech.agrowerk.infrastructure.model.barter.BarterOffer;
import tech.agrowerk.infrastructure.model.barter.BarterTransaction;
import tech.agrowerk.infrastructure.model.barter.CropCommitment;
import tech.agrowerk.infrastructure.model.barter.enums.CommitmentStatus;
import tech.agrowerk.infrastructure.model.barter.enums.ContractStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.TransactionStatus;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.repository.barter.BarterContractRepository;
import tech.agrowerk.infrastructure.repository.barter.BarterOfferRepository;
import tech.agrowerk.infrastructure.repository.barter.BarterTransactionRepository;
import tech.agrowerk.infrastructure.repository.barter.CropCommitmentRepository;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;

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
    private final CropCommitmentRepository cropCommitmentRepository;
    private final UserRepository userRepository;
    private final CropRepository cropRepository;
    private final BarterTransactionMapper barterTransactionMapper;
    private final AuthUtil authUtil;


    public BarterTransactionService(BarterTransactionRepository barterTransactionRepository,
                                    BarterOfferRepository barterOfferRepository,
                                    BarterContractRepository barterContractRepository,
                                    CropCommitmentRepository cropCommitmentRepository,
                                    UserRepository userRepository,
                                    CropRepository cropRepository,
                                    BarterTransactionMapper barterTransactionMapper,
                                    AuthUtil authUtil) {
        this.barterTransactionRepository = barterTransactionRepository;
        this.barterOfferRepository = barterOfferRepository;
        this.barterContractRepository = barterContractRepository;
        this.cropCommitmentRepository = cropCommitmentRepository;
        this.userRepository = userRepository;
        this.cropRepository = cropRepository;
        this.barterTransactionMapper = barterTransactionMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public BarterTransactionResponse proposeTransaction(ProposeTransactionRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterOffer offer = barterOfferRepository.findById(request.offerId())
                .orElseThrow(() -> new EntityNotFoundException("Offer not found"));

        if (offer.getStatus() != OfferStatus.ACTIVE)
            throw new OperationDeniedException("Offer is not active");

        if (offer.getOwner().getId().equals(auth.id()))
            throw new OperationDeniedException("You cannot propose on your own offer");

        boolean alreadyProposed = barterTransactionRepository.existsByOffer_IdAndOfferor_IdAndStatusIn(
                offer.getId(), auth.id(),
                List.of(TransactionStatus.PENDING, TransactionStatus.CONFIRMED)
        );
        if (alreadyProposed)
            throw new OperationDeniedException("You already have an active proposal for this offer");

        User offeror  = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        BarterTransaction transaction = BarterTransaction.builder()
                .offer(offer)
                .offeror(offeror)
                .acceptor(offer.getOwner())
                .offerorGives(request.offerorGives())
                .offerorCropQuantity(request.offerorCropQuantity())
                .offerorAssetQuantity(request.offerorAssetQuantity())
                .acceptorGives(offer.getOfferType())
                .acceptorCropQuantity(offer.getOfferedCropQuantity())
                .acceptorCrop(offer.getOfferedCrop())
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
    public BarterTransactionResponse acceptTransaction(UUID transactionId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        BarterTransaction transaction = findTransactionAndValidateAcceptor(transactionId, auth.id());

        if (transaction.getStatus() != TransactionStatus.PENDING)
            throw new OperationDeniedException("Only PENDING transactions can be accepted");

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setUpdatedAt(LocalDateTime.now());

        transaction.getOffer().setStatus(OfferStatus.ACCEPTED);

        barterTransactionRepository.cancelAllPendingExcept(
                transaction.getOffer().getId(), transactionId);

        generateCommitments(transaction);

        generateContract(transaction);

        log.info("Transaction accepted id={} — other proposals cancelled", transactionId);
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

        t.setStatus(TransactionStatus.CANCELLED);
        t.setUpdatedAt(LocalDateTime.now());

        if (t.getStatus() == TransactionStatus.CONFIRMED) {
            t.getOffer().setStatus(OfferStatus.ACTIVE);
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

    private void generateCommitments(BarterTransaction t) {
        if (t.getOfferorCrop() != null) {
            cropCommitmentRepository.save(CropCommitment.builder()
                    .transaction(t)
                    .farmer(t.getOfferor())
                    .crop(t.getOfferorCrop())
                    .committedQuantity(t.getOfferorCropQuantity())
                    .deliveredQuantity(java.math.BigDecimal.ZERO)
                    .expectedDeliveryDate(t.getOfferorDeliveryDate())
                    .status(CommitmentStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        if (t.getAcceptorCrop() != null) {
            cropCommitmentRepository.save(CropCommitment.builder()
                    .transaction(t)
                    .farmer(t.getAcceptor())
                    .crop(t.getAcceptorCrop())
                    .committedQuantity(t.getAcceptorCropQuantity())
                    .deliveredQuantity(java.math.BigDecimal.ZERO)
                    .expectedDeliveryDate(t.getAcceptorDeliveryDate())
                    .status(CommitmentStatus.CONFIRMED)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        log.info("Commitments generated for transaction={}", t.getId());
    }

    private void generateContract(BarterTransaction t) {
        String contractNumber = "BC-" + System.currentTimeMillis();

        LocalDate endDate = t.getAcceptorDeliveryDate().isAfter(t.getOfferorDeliveryDate())
                ? t.getAcceptorDeliveryDate()
                : t.getOfferorDeliveryDate();

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
        log.info("Contract generated id={} number={} status=AWAITING_OFFEROR_SIGNATURE",
                contract.getId(), contractNumber);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
