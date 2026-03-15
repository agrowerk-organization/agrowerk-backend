package tech.agrowerk.business.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.listener.events.PlantingInputConsumedEvent;
import tech.agrowerk.business.listener.events.StockAdjustmentEvent;
import tech.agrowerk.business.listener.events.StockTransferEvent;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class StockEventListener {

    private final StockMovementRepository movementRepository;
    private final StockRepository stockRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public StockEventListener(StockMovementRepository movementRepository, StockRepository stockRepository, PropertyRepository propertyRepository, UserRepository userRepository) {
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void onStockAdjustment(StockAdjustmentEvent event) {
        Stock stock = stockRepository.findById(event.stockId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock not found"));

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setProperty(propertyRepository
                .getReferenceById(event.propertyId()));
        movement.setUser(userRepository
                .getReferenceById(event.userId()));
        movement.setQuantity(event.quantity());
        movement.setMovementType(event.adjustmentType());
        movement.setMovementDate(LocalDateTime.now());
        movement.setJustification(event.justification());
        movement.setDocumentNumber(event.documentNumber());
        movement.setNotes("Manual adjustment");
        movementRepository.save(movement);

        log.info("StockMovement created for adjustment " +
                        "stock={} type={}",
                event.stockId(), event.adjustmentType());
    }

    @EventListener
    @Transactional
    public void onStockTransfer(StockTransferEvent event) {

        Stock sourceStock = stockRepository
                .findById(event.sourceStockId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Source stock not found"));

        StockMovement outMovement = new StockMovement();
        outMovement.setStock(sourceStock);
        outMovement.setProperty(propertyRepository
                .getReferenceById(event.sourcePropertyId()));
        outMovement.setUser(userRepository
                .getReferenceById(event.userId()));
        outMovement.setQuantity(event.quantity());
        outMovement.setUnitValue(event.weightedAverageCost());
        BigDecimal multiply = event.quantity()
                .multiply(event.weightedAverageCost());
        outMovement.setTotalValue(multiply);
        outMovement.setMovementType(MovementType.TRANSFER_OUT);
        outMovement.setMovementDate(LocalDateTime.now());
        outMovement.setDestination(
                event.targetPropertyId().toString());
        outMovement.setNotes("Transfer to property " +
                event.targetPropertyId());
        movementRepository.save(outMovement);

        Stock targetStock = stockRepository
                .findById(event.targetStockId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Target stock not found"));

        StockMovement inMovement = new StockMovement();
        inMovement.setStock(targetStock);
        inMovement.setProperty(propertyRepository
                .getReferenceById(event.targetPropertyId()));
        inMovement.setUser(userRepository
                .getReferenceById(event.userId()));
        inMovement.setQuantity(event.quantity());
        inMovement.setUnitValue(event.weightedAverageCost());
        inMovement.setTotalValue(multiply);
        inMovement.setMovementType(MovementType.TRANSFER_IN);
        inMovement.setMovementDate(LocalDateTime.now());
        inMovement.setDestination(
                event.sourcePropertyId().toString());
        inMovement.setNotes("Transfer from property " +
                event.sourcePropertyId());
        movementRepository.save(inMovement);

        log.info("StockMovements created for transfer " +
                        "from={} to={} quantity={}",
                event.sourcePropertyId(),
                event.targetPropertyId(),
                event.quantity());
    }

    @EventListener
    @Transactional
    public void onPlantingInputConsumed(PlantingInputConsumedEvent event) {

        Stock stock = stockRepository
                .findByProperty_IdAndInput_IdAndStockType(
                        event.propertyId(),
                        event.inputId(),
                        StockType.INPUT)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Stock not found"));

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setProperty(propertyRepository
                .getReferenceById(event.propertyId()));
        movement.setUser(userRepository
                .getReferenceById(event.userId()));
        movement.setQuantity(event.quantity());
        movement.setUnitValue(event.unitPrice());
        movement.setTotalValue(event.quantity()
                .multiply(event.unitPrice()));
        movement.setMovementType(MovementType.PLANTING_USE);
        movement.setMovementDate(LocalDateTime.now());
        movement.setCrop(event.cropName());
        movement.setNotes("FEFO consumption — batch: " +
                event.batchId());
        movementRepository.save(movement);

        log.info("StockMovement created for FEFO consumption " +
                        "input={} batch={} quantity={}",
                event.inputId(),
                event.batchId(),
                event.quantity());
    }
}