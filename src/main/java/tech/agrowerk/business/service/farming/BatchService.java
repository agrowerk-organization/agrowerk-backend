package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.farming.CreateBatchRequest;
import tech.agrowerk.application.dto.request.farming.ReceiveBatchRequest;
import tech.agrowerk.application.dto.response.farming.BatchResponse;
import tech.agrowerk.business.listener.events.BatchCreatedEvent;
import tech.agrowerk.business.listener.events.BatchReceivedEvent;
import tech.agrowerk.business.mapper.farming.BatchMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.enums.BatchReceiptStatus;
import tech.agrowerk.infrastructure.model.farming.enums.BatchStatus;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.BatchRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;
import tech.agrowerk.infrastructure.repository.supplier.SupplierRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class BatchService {
    private final BatchRepository batchRepository;
    private final InputRepository inputRepository;
    private final SupplierRepository supplierRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final BatchMapper batchMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    public BatchService(BatchRepository batchRepository,
                        InputRepository inputRepository,
                        SupplierRepository supplierRepository,
                        PropertyRepository propertyRepository,
                        UserRepository userRepository,
                        UserPropertyRepository userPropertyRepository,
                        BatchMapper batchMapper,
                        OwnershipValidator ownershipValidator,
                        AuthUtil authUtil,
                        ApplicationEventPublisher applicationEventPublisher) {
        this.batchRepository = batchRepository;
        this.inputRepository = inputRepository;
        this.supplierRepository = supplierRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.batchMapper = batchMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Input input = inputRepository.findById(request.inputId())
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.isSupplierAdmin()) {
            if (!supplier.getAdministrator().getId().equals(auth.id())) {
                throw new AccessDeniedException(
                        "Supplier admin can only create batch for their own supplier"
                );
            }
        }

        if (batchRepository.existsByBatchNumber(request.batchNumber())) {
            throw new EntityAlreadyExistsException("Batch number already exists");
        }

        if (request.manufacturingDate().isAfter(request.expirationDate())) {
            throw new IllegalArgumentException(
                    "Manufacturing date cannot be after expiration date"
            );
        }

        if (request.expirationDate().isBefore(LocalDate.now())) {
            log.warn("Batch {} is already expired on entry", request.batchNumber());
        }

        Batch batch = batchMapper.toEntity(request, input, supplier);
        Batch saved = batchRepository.save(batch);

        applicationEventPublisher.publishEvent(new BatchCreatedEvent(
                saved.getId(),
                saved.getInput().getId(),
                saved.getSupplier().getId(),
                saved.getInitialQuantity(),
                saved.getUnitPrice(),
                saved.getExpirationDate()
        ));

        log.info("Batch created id={} input={} supplier={}",
                saved.getId(), request.inputId(), request.supplierId());

        return batchMapper.toResponse(saved);
    }

    @Transactional
    public BatchResponse receiveBatch(UUID batchId, ReceiveBatchRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));

        ownershipValidator.validateOwnership(request.propertyId(), auth.id());

        if (batch.getReceiptStatus() != BatchReceiptStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING batches can be received"
            );
        }

        if (batch.isExpired()) {
            throw new IllegalArgumentException(
                    "Cannot receive an expired batch"
            );
        }

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        batch.setProperty(property);
        batch.setReceiptStatus(BatchReceiptStatus.RECEIVED);
        batch.setReceivedAt(LocalDateTime.now());
        batch.setReceivedBy(user);

        applicationEventPublisher.publishEvent(new BatchReceivedEvent(
                batch.getId(),
                batch.getInput().getId(),
                request.propertyId(),
                auth.id(),
                request.warehouseId(),
                batch.getCurrentQuantity(),
                batch.getUnitPrice(),
                batch.getTotalValue()
        ));

        log.info("Batch received id={} property={}",
                batchId, request.propertyId());

        return batchMapper.toResponse(batch);
    }

    @Transactional
    public BatchResponse cancelBatch(UUID batchId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (batch.getReceiptStatus() != BatchReceiptStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING batches can be cancelled"
            );
        }

        if (user.isSupplierAdmin()) {
            if (!batch.getSupplier().getAdministrator().getId().equals(auth.id())) {
                throw new AccessDeniedException(
                        "Supplier admin can only cancel their own batches"
                );
            }
        }

        batch.setReceiptStatus(BatchReceiptStatus.CANCELLED);
        batch.setStatus(BatchStatus.EXPIRED);

        log.info("Batch cancelled id={}", batchId);
        return batchMapper.toResponse(batch);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findBySupplier(UUID supplierId, Pageable pageable) {
        return batchRepository.findBySupplier_Id(supplierId, pageable)
                .map(batchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findByInput(UUID inputId, Pageable pageable) {
        return batchRepository.findByInput_Id(inputId, pageable)
                .map(batchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findByProperty(UUID propertyId, Pageable pageable) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return batchRepository.findByProperty_Id(propertyId, pageable)
                .map(batchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findNearExpiration(
            UUID propertyId, int daysAlert, Pageable pageable) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        LocalDate alertDate = LocalDate.now().plusDays(daysAlert);

        return batchRepository.findNearExpirationByProperty(
                        propertyId, BatchStatus.IN_USE, alertDate, pageable)
                .map(batchMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<BatchResponse> findExpired(UUID propertyId, Pageable pageable) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return batchRepository.findExpiredWithRemainingStock(
                        propertyId, BatchStatus.IN_USE, pageable)
                .map(batchMapper::toResponse);
    }
}
