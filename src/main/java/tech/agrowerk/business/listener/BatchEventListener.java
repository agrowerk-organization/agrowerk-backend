package tech.agrowerk.business.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.listener.events.BatchCreatedEvent;
import tech.agrowerk.business.listener.events.BatchReceivedEvent;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.Warehouse;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.BatchRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;
import tech.agrowerk.infrastructure.repository.inventory.WarehouseRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@Slf4j
public class BatchEventListener {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final BatchRepository batchRepository;
    private final InputRepository inputRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;

    public BatchEventListener(StockRepository stockRepository,
                              StockMovementRepository stockMovementRepository,
                              BatchRepository batchRepository,
                              InputRepository inputRepository,
                              PropertyRepository propertyRepository,
                              UserRepository userRepository,
                              WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.batchRepository = batchRepository;
        this.inputRepository = inputRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @EventListener
    @Transactional
    public void onBatchCreated(BatchCreatedEvent event) {
        if (!event.isBarter()) return;

        Input input = inputRepository.findById(event.inputId())
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        input.setLastPurchasePrice(event.unitPrice());

        CmpAccumulator result = batchRepository.findAllActiveBarterPendingOrReceivedByInputId(event.inputId())
                .stream()
                .reduce(
                        new CmpAccumulator(BigDecimal.ZERO, BigDecimal.ZERO),
                        (acc, b) -> new CmpAccumulator(
                                acc.totalValue().add(b.getUnitPrice().multiply(b.getCurrentQuantity())),
                                acc.totalQuantity().add(b.getCurrentQuantity())
                        ),
                        (a, b) -> new CmpAccumulator(a.totalValue().add(b.totalValue()), a.totalQuantity().add(b.totalQuantity()))
                );

        input.setAveragePurchasePrice(result.calculate());

        inputRepository.save(input);
        log.info("Barter CMP recalculated for input {}: {}",
                input.getName(), input.getAveragePurchasePrice());

    }

    @EventListener
    @Transactional
    public void onBatchReceived(BatchReceivedEvent event) {
        Input input = inputRepository.findById(event.inputId())
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        if (event.warehouseId() != null) {
            validateAndUpdateWarehouse(event);
        }

        Stock stock = stockRepository
                .findByProperty_IdAndInput_IdAndStockType(
                        event.propertyId(),
                        event.inputId(),
                        StockType.INPUT)
                .orElseGet(() -> createInputStock(event, input));

        BigDecimal newQuantity = stock.getCurrentQuantity()
                .add(event.quantity());

        BigDecimal currentTotalValue = stock.getTotalValue() != null
                ? stock.getTotalValue() : BigDecimal.ZERO;

        BigDecimal newTotalValue = currentTotalValue.add(event.totalValue());

        BigDecimal newCMP = BigDecimal.ZERO;
        if (newQuantity.compareTo(BigDecimal.ZERO) > 0) {
            newCMP = newTotalValue.divide(newQuantity, 2, RoundingMode.HALF_UP);
        }

        stock.setCurrentQuantity(newQuantity);
        stock.setTotalValue(newTotalValue);
        stock.setWeightedAverageCost(newCMP);
        stock.setLastEntryDate(LocalDateTime.now());

        if (event.warehouseId() != null) {
            stock.setWarehouse(warehouseRepository
                    .getReferenceById(event.warehouseId()));
        }

        input.setAveragePurchasePrice(newCMP);
        input.setLastPurchasePrice(event.unitPrice());

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setProperty(propertyRepository
                .getReferenceById(event.propertyId()));
        movement.setUser(userRepository
                .getReferenceById(event.receivedBy()));
        movement.setQuantity(event.quantity());
        movement.setUnitValue(event.unitPrice());
        movement.setTotalValue(event.totalValue());
        movement.setMovementType(MovementType.PURCHASE);
        movement.setMovementDate(LocalDateTime.now());
        movement.setNotes("Batch received id=" + event.batchId());
        stockMovementRepository.save(movement);
        inputRepository.save(input);

        log.info("Stock updated after batch received id={} property={}",
                event.batchId(), event.propertyId());
    }

    private void validateAndUpdateWarehouse(BatchReceivedEvent event) {
        Warehouse warehouse = warehouseRepository
                .findById(event.warehouseId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Warehouse not found"));

        if (!warehouse.getProperty().getId().equals(event.propertyId())) {
            throw new IllegalArgumentException(
                    "Warehouse does not belong to this property"
            );
        }

        if (!warehouse.getIsActive()) {
            throw new IllegalArgumentException(
                    "Warehouse is not active"
            );
        }

        if (warehouse.getCapacityKg() != null) {
            BigDecimal currentOccupancy = warehouse.getCurrentOccupancyKg() != null
                    ? warehouse.getCurrentOccupancyKg()
                    : BigDecimal.ZERO;

            BigDecimal newOccupancy = currentOccupancy.add(event.quantity());

            if (newOccupancy.compareTo(warehouse.getCapacityKg()) > 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "Warehouse capacity exceeded — capacity: %.2fkg, " +
                                        "current: %.2fkg, incoming: %.2fkg",
                                warehouse.getCapacityKg(),
                                currentOccupancy,
                                event.quantity())
                );
            }

            warehouse.setCurrentOccupancyKg(newOccupancy);
            log.info("Warehouse id={} occupancy updated to {}kg",
                    event.warehouseId(), newOccupancy);
        }
    }

    private Stock createInputStock(BatchReceivedEvent event, Input input) {
        Stock stock = new Stock();
        stock.setProperty(propertyRepository.getReferenceById(event.propertyId()));
        stock.setInput(input);
        stock.setStockType(StockType.INPUT);
        stock.setCurrentQuantity(BigDecimal.ZERO);
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setTotalValue(BigDecimal.ZERO);
        stock.setWeightedAverageCost(BigDecimal.ZERO);
        return stockRepository.save(stock);
    }

    public record CmpAccumulator(BigDecimal totalValue, BigDecimal totalQuantity) {
        public BigDecimal calculate() {
            return totalQuantity.compareTo(BigDecimal.ZERO) > 0
                    ? totalValue.divide(totalQuantity, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }
    }
}