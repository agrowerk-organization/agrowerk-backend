package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.inventory.StockAdjustmentRequest;
import tech.agrowerk.application.dto.request.inventory.StockTransferRequest;
import tech.agrowerk.application.dto.response.inventory.StockResponse;
import tech.agrowerk.business.listener.events.StockAdjustmentEvent;
import tech.agrowerk.business.listener.events.StockTransferEvent;
import tech.agrowerk.business.mapper.inventory.StockMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@Slf4j
public class StockService {

    private final StockRepository stockRepository;
    private final PropertyRepository propertyRepository;
    private final InputRepository inputRepository;
    private final StockMapper stockMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    public StockService(StockRepository stockRepository,
                        StockMovementRepository stockMovementRepository,
                        PropertyRepository propertyRepository,
                        InputRepository inputRepository,
                        UserRepository userRepository,
                        UserPropertyRepository userPropertyRepository,
                        StockMapper stockMapper,
                        OwnershipValidator ownershipValidator,
                        AuthUtil authUtil,
                        ApplicationEventPublisher applicationEventPublisher) {
        this.stockRepository = stockRepository;
        this.propertyRepository = propertyRepository;
        this.inputRepository = inputRepository;
        this.stockMapper = stockMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findByProperty(UUID propertyId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        return stockRepository.findByProperty_Id(propertyId)
                .stream()
                .map(stockMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> findAlerts(UUID propertyId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        return mergeAlertStocks(propertyId)
                .map(stockMapper::toResponse)
                .toList();
    }

    @Transactional
    public StockResponse adjustStock(StockAdjustmentRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Stock stock = findStockById(request.stockId());
        ownershipValidator.validateMasterOwnership(stock.getProperty().getId(), auth.id());

        validateAdjustmentType(request.adjustmentType());
        applyAdjustment(stock, request.adjustmentType(), request.quantity());

        publishAdjustmentEvent(stock, auth.id(), request);

        return stockMapper.toResponse(stock);
    }

    @Transactional
    public void transferStock(StockTransferRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        validateTransferOwnership(request, auth.id());
        validateDifferentProperties(request);

        Input input = findInputById(request.inputId());

        Stock sourceStock = findSourceStock(request);
        validateTransferQuantity(request.quantity(), sourceStock);

        Stock targetStock = resolveTargetStock(request, input);

        applyTransfer(sourceStock, targetStock, request.quantity());

        publishTransferEvent(sourceStock, targetStock, request, auth.id());

        log.info("Stock transferred input={} quantity={} from={} to={}",
                request.inputId(), request.quantity(),
                request.sourcePropertyId(), request.targetPropertyId());
    }

    private Stock findStockById(UUID stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));
    }

    private Input findInputById(UUID inputId) {
        return inputRepository.findById(inputId)
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));
    }

    private Stock findSourceStock(StockTransferRequest request) {
        return stockRepository
                .findByProperty_IdAndInput_IdAndStockType(
                        request.sourcePropertyId(),
                        request.inputId(),
                        StockType.INPUT)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock not found in source property"));
    }

    private Stock resolveTargetStock(StockTransferRequest request, Input input) {
        return stockRepository
                .findByProperty_IdAndInput_IdAndStockType(
                        request.targetPropertyId(),
                        request.inputId(),
                        StockType.INPUT)
                .orElseGet(() -> createInputStock(request.targetPropertyId(), input));
    }

    private Stream<Stock> mergeAlertStocks(UUID propertyId) {
        List<Stock> belowMin = stockRepository.findBelowMinimumByProperty(propertyId);
        List<Stock> aboveMax = stockRepository.findAboveMaximumByProperty(propertyId);
        return Stream.concat(belowMin.stream(), aboveMax.stream());
    }


    private void validateAdjustmentType(MovementType type) {
        if (type != MovementType.POSITIVE_ADJUSTMENT &&
                type != MovementType.NEGATIVE_ADJUSTMENT) {
            throw new IllegalArgumentException(
                    "Invalid adjustment type — use POSITIVE_ADJUSTMENT or NEGATIVE_ADJUSTMENT");
        }
    }

    private void validateTransferOwnership(StockTransferRequest request, UUID userId) {
        ownershipValidator.validateOwnership(request.sourcePropertyId(), userId);
        ownershipValidator.validateOwnership(request.targetPropertyId(), userId);
    }

    private void validateDifferentProperties(StockTransferRequest request) {
        if (request.sourcePropertyId().equals(request.targetPropertyId())) {
            throw new IllegalArgumentException(
                    "Source and target properties must be different");
        }
    }

    private void validateTransferQuantity(BigDecimal quantity, Stock sourceStock) {
        if (quantity.compareTo(sourceStock.getAvailableQuantity()) > 0) {
            throw new IllegalArgumentException(
                    String.format("Transfer quantity %.3f exceeds available stock %.3f",
                            quantity, sourceStock.getAvailableQuantity()));
        }
    }

    private void applyAdjustment(Stock stock, MovementType type, BigDecimal quantity) {
        if (type == MovementType.NEGATIVE_ADJUSTMENT) {
            validateNegativeAdjustment(stock, quantity);
            stock.setCurrentQuantity(stock.getCurrentQuantity().subtract(quantity));
            stock.setLastExitDate(LocalDateTime.now());
        } else {
            stock.setCurrentQuantity(stock.getCurrentQuantity().add(quantity));
            stock.setLastEntryDate(LocalDateTime.now());
        }
    }

    private void validateNegativeAdjustment(Stock stock, BigDecimal quantity) {
        if (quantity.compareTo(stock.getAvailableQuantity()) > 0) {
            throw new IllegalArgumentException(
                    String.format("Adjustment quantity %.3f exceeds available stock %.3f",
                            quantity, stock.getAvailableQuantity()));
        }
    }

    private void applyTransfer(Stock source, Stock target, BigDecimal quantity) {
        source.setCurrentQuantity(source.getCurrentQuantity().subtract(quantity));
        source.setLastExitDate(LocalDateTime.now());

        target.setCurrentQuantity(target.getCurrentQuantity().add(quantity));
        target.setLastEntryDate(LocalDateTime.now());
        target.setWeightedAverageCost(source.getWeightedAverageCost());
    }

    private Stock createInputStock(UUID propertyId, Input input) {
        Stock stock = new Stock();
        stock.setProperty(propertyRepository.getReferenceById(propertyId));
        stock.setInput(input);
        stock.setStockType(StockType.INPUT);
        stock.setCurrentQuantity(BigDecimal.ZERO);
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setTotalValue(BigDecimal.ZERO);
        stock.setWeightedAverageCost(BigDecimal.ZERO);
        return stockRepository.save(stock);
    }

    private void publishAdjustmentEvent(Stock stock, UUID userId, StockAdjustmentRequest request) {
        applicationEventPublisher.publishEvent(new StockAdjustmentEvent(
                stock.getId(),
                stock.getProperty().getId(),
                userId,
                request.quantity(),
                request.adjustmentType(),
                request.justification(),
                request.documentNumber()
        ));
    }

    private void publishTransferEvent(Stock source, Stock target,
                                      StockTransferRequest request, UUID userId) {
        applicationEventPublisher.publishEvent(new StockTransferEvent(
                source.getId(),
                target.getId(),
                request.sourcePropertyId(),
                request.targetPropertyId(),
                request.inputId(),
                userId,
                request.quantity(),
                source.getWeightedAverageCost(),
                request.justification()
        ));
    }
}