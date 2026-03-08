package tech.agrowerk.business.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.listener.events.HarvestFinalizedEvent;
import tech.agrowerk.business.listener.events.HarvestPartialAddedEvent;
import tech.agrowerk.business.listener.events.HarvestPartialUpdatedEvent;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.farming.Field;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.Yield;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.*;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class HarvestEventListener {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final YieldRepository yieldRepository;
    private final PlantingRepository plantingRepository;
    private final PropertyRepository propertyRepository;
    private final FieldRepository fieldRepository;
    private final HarvestRepository harvestRepository;
    private final HarvestPartialRepository harvestPartialRepository;
    private final UserRepository userRepository;

    public HarvestEventListener(StockRepository stockRepository,
                                StockMovementRepository movementRepository,
                                YieldRepository yieldRepository,
                                PlantingRepository plantingRepository,
                                PropertyRepository propertyRepository,
                                FieldRepository fieldRepository,
                                HarvestRepository harvestRepository,
                                HarvestPartialRepository harvestPartialRepository,
                                UserRepository userRepository) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.yieldRepository = yieldRepository;
        this.plantingRepository = plantingRepository;
        this.propertyRepository = propertyRepository;
        this.fieldRepository = fieldRepository;
        this.harvestRepository = harvestRepository;
        this.harvestPartialRepository = harvestPartialRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void onPartialAdded(HarvestPartialAddedEvent event) {
        Stock stock = stockRepository
                .findByProperty_IdAndStockType(
                        event.propertyId(), event.inputId(), StockType.PRODUCTION)
                .orElseGet(() -> createProductionStock(event.propertyId()));

        stock.setCurrentQuantity(
                stock.getCurrentQuantity().add(event.quantityKg()));
        stock.setLastEntryDate(LocalDateTime.now());

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setProperty(stock.getProperty());
        movement.setQuantity(event.quantityKg());
        movement.setMovementType(MovementType.HARVEST_IN);
        movement.setMovementDate(LocalDateTime.now());
        movement.setCrop(event.cropName());
        movement.setNotes("Partial harvest id=" + event.partialId());
        movementRepository.save(movement);

        log.info("Stock updated after partial harvest id={}", event.partialId());
    }

    @EventListener
    @Transactional
    public void onPartialUpdate(HarvestPartialUpdatedEvent event) {
        BigDecimal difference = event.newQuantityKg()
                .subtract(event.previousQuantityKg());

        if (difference.compareTo(BigDecimal.ZERO) == 0) return;

        Stock stock = stockRepository
                .findByProperty_IdAndStockType(
                        event.propertyId(), event.inputId(), StockType.PRODUCTION)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        stock.setCurrentQuantity(
                stock.getCurrentQuantity().add(difference));

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setQuantity(difference.abs());
        movement.setMovementType(difference.compareTo(BigDecimal.ZERO) > 0
                ? MovementType.POSITIVE_ADJUSTMENT
                : MovementType.NEGATIVE_ADJUSTMENT);
        movement.setNotes("Partial harvest correction id=" + event.partialId());
        movement.setMovementDate(LocalDateTime.now());
        movementRepository.save(movement);
    }

    @EventListener
    @Transactional
    public void onHarvestFinalized(HarvestFinalizedEvent event) {
        updateCMP(event);
        createYield(event);
        updateStatuses(event);
    }

    private void updateCMP(HarvestFinalizedEvent event) {
        Stock stock = stockRepository
                .findByProperty_IdAndStockType(
                        event.propertyId(), event.inputId(), StockType.PRODUCTION)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        BigDecimal newTotalValue = stock.getTotalValue()
                .add(event.totalPlantingCost());

        BigDecimal newCMP = newTotalValue.divide(
                stock.getCurrentQuantity(), 2, RoundingMode.HALF_UP);

        stock.setTotalValue(newTotalValue);
        stock.setWeightedAverageCost(newCMP);

        log.info("CMP updated after harvest finalization id={}", event.harvestId());
    }

    private void createYield(HarvestFinalizedEvent event) {
        BigDecimal productivity = event.totalQuantityKg()
                .divide(event.areaHectares(), 2, RoundingMode.HALF_UP);

        Yield yield = new Yield();
        yield.setHarvest(harvestRepository.getReferenceById(event.harvestId()));
        yield.setField(fieldRepository.getReferenceById(event.fieldId()));
        yield.setTotalProducedKg(event.totalQuantityKg());
        yield.setProductivityPerHectare(productivity);
        yield.setCreatedAt(Instant.now());
        yield.setUpdatedAt(Instant.now());
        yieldRepository.save(yield);

        log.info("Yield created after harvest finalization id={}", event.harvestId());
    }

    private void updateStatuses(HarvestFinalizedEvent event) {
        Planting planting = plantingRepository.findById(event.plantingId())
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        planting.setPlantingStatus(PlantingStatus.HARVESTED);

        boolean hasOtherActive = plantingRepository
                .existsByField_IdAndPlantingStatus(
                        event.fieldId(), PlantingStatus.IN_PROGRESS);

        if (!hasOtherActive) {
            Field field = fieldRepository.findById(event.fieldId())
                    .orElseThrow(() -> new EntityNotFoundException("Field not found"));
            field.setFieldStatus(FieldStatus.INACTIVE);
            log.info("Field id={} set to INACTIVE", event.fieldId());
        }
    }

    private Stock createProductionStock(UUID propertyId) {
        Stock stock = new Stock();
        stock.setProperty(propertyRepository.getReferenceById(propertyId));
        stock.setStockType(StockType.PRODUCTION);
        stock.setCurrentQuantity(BigDecimal.ZERO);
        stock.setReservedQuantity(BigDecimal.ZERO);
        stock.setTotalValue(BigDecimal.ZERO);
        stock.setWeightedAverageCost(BigDecimal.ZERO);
        return stockRepository.save(stock);
    }
}