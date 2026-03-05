package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.create.CreatePlantingInputRequest;
import tech.agrowerk.application.dto.response.PlantingInputResponse;
import tech.agrowerk.business.mapper.PlantingInputMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.exception.local.InsufficientStockException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.PlantingInput;
import tech.agrowerk.infrastructure.model.farming.enums.BatchStatus;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.BatchRepository;
import tech.agrowerk.infrastructure.repository.farming.PlantingInputRepository;
import tech.agrowerk.infrastructure.repository.farming.PlantingRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PlantingInputService {

    private final PlantingInputRepository plantingInputRepository;
    private final PlantingRepository plantingRepository;
    private final InputRepository inputRepository;
    private final StockRepository stockRepository;
    private final BatchRepository batchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final PlantingInputMapper plantingInputMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public PlantingInputService(PlantingInputRepository plantingInputRepository,
                                PlantingRepository plantingRepository,
                                InputRepository inputRepository,
                                StockRepository stockRepository,
                                BatchRepository batchRepository,
                                StockMovementRepository stockMovementRepository,
                                UserRepository userRepository, PlantingInputMapper plantingInputMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.plantingInputRepository = plantingInputRepository;
        this.plantingRepository = plantingRepository;
        this.inputRepository = inputRepository;
        this.stockRepository = stockRepository;
        this.batchRepository = batchRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.userRepository = userRepository;
        this.plantingInputMapper = plantingInputMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional
    public PlantingInputResponse createInput(CreatePlantingInputRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(request.plantingId())
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Planting is not in progress");
        }

        Input input = inputRepository.findById(request.inputId())
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        Stock stock = stockRepository.findByPropertyIdAndInputId(planting.getProperty().getId(), request.inputId());

        if (stock.getAvailableQuantity().compareTo(request.quantity()) < 0) {
            throw new InsufficientStockException(
                    String.format("Insufficient stock — available: %.3f, requested: %.3f",
                            stock.getAvailableQuantity(), request.quantity())
            );
        }

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        consumeStockFEFO(planting, input, stock, request.quantity(), user);

        PlantingInput plantingInput = plantingInputMapper.toEntity(request, planting, input);
        PlantingInput saved = plantingInputRepository.save(plantingInput);

        log.info("PlantingInput registered id={} planting={} input={} quantity={}",
                saved.getId(), request.plantingId(), request.inputId(), request.quantity());

        return plantingInputMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PlantingInputResponse> findByPlanting(UUID plantingId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());

        return plantingInputRepository.findByPlantingId(plantingId, pageable)
                .map(plantingInputMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantingInputResponse> findByInput(UUID inputId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Input input = inputRepository.findById(inputId)
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        Page<PlantingInput> plantingInputs = plantingInputRepository.findByInputId(inputId, pageable);

        return plantingInputs
                .map(pi -> {
                    ownershipValidator.validateOwnership(pi.getPlanting().getProperty().getId(), auth.id());
                    return plantingInputMapper.toResponse(pi);
                });
    }

    private void consumeStockFEFO(Planting planting, Input input,
                                  Stock stock, BigDecimal quantityNeeded,
                                  User user) {

        List<Batch> batches = batchRepository
                .findActiveByInputOrderByExpirationDateAsc(
                        input.getId(), BatchStatus.AVAILABLE);

        BigDecimal remaining = quantityNeeded;

        for (Batch batch : batches) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            if (batch.isExpired()) {
                batch.setStatus(BatchStatus.EXPIRED);
                log.warn("Batch id={} marked as EXPIRED during FEFO consumption", batch.getId());
                continue;
            }

            BigDecimal toConsume = remaining.min(batch.getCurrentQuantity());

            batch.setCurrentQuantity(batch.getCurrentQuantity().subtract(toConsume));
            if (batch.getCurrentQuantity().compareTo(BigDecimal.ZERO) == 0) {
                batch.setStatus(BatchStatus.DEPLETED);
                log.info("Batch id={} fully depleted", batch.getId());
            }

            remaining = remaining.subtract(toConsume);

            StockMovement movement = new StockMovement();
            movement.setStock(stock);
            movement.setProperty(planting.getProperty());
            movement.setBatch(batch);
            movement.setUser(user);
            movement.setQuantity(toConsume);
            movement.setUnitValue(batch.getUnitPrice());
            movement.setTotalValue(toConsume.multiply(batch.getUnitPrice()));
            movement.setMovementType(MovementType.PLANTING_USE);
            movement.setMovementDate(LocalDateTime.now());
            movement.setCrop(planting.getCropVariety().getCrop().getName());
            movement.setNotes("FEFO consumption — batch: " + batch.getBatchNumber());
            stockMovementRepository.save(movement);
        }

        stock.setCurrentQuantity(stock.getCurrentQuantity().subtract(quantityNeeded));
        stock.setLastExitDate(LocalDateTime.now());
    }
}
