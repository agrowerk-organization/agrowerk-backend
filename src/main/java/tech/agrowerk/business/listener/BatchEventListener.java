package tech.agrowerk.business.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.listener.events.BatchReceivedEvent;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.inventory.InputRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementRepository;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@Slf4j
public class BatchEventListener {

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InputRepository inputRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public BatchEventListener(StockRepository stockRepository,
                              StockMovementRepository stockMovementRepository,
                              InputRepository inputRepository,
                              PropertyRepository propertyRepository,
                              UserRepository userRepository) {
        this.stockRepository = stockRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.inputRepository = inputRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    @EventListener
    @Transactional
    public void onBatchREceived(BatchReceivedEvent event) {
        Input input = inputRepository.findById(event.inputId())
                .orElseThrow(() -> new EntityNotFoundException("Input not found"));

        Stock stock = stockRepository.findByProperty_IdAndInput_IdAndStockType(
                event.propertyId(),
                event.inputId(),
                StockType.INPUT
        ).orElseGet(() -> createInputStock(event, input));

        BigDecimal newQuantity = stock.getCurrentQuantity()
                .add(event.quantity());

        BigDecimal currentTotalValue = stock.getTotalValue() != null
                ? stock.getTotalValue() : BigDecimal.ZERO;

        BigDecimal newTotalValue = currentTotalValue.add(event.totalValue());

        BigDecimal newCMP = newTotalValue.divide(
                newQuantity, 2, RoundingMode.HALF_UP);

        stock.setCurrentQuantity(newQuantity);
        stock.setTotalValue(newTotalValue);
        stock.setWeightedAverageCost(newCMP);
        stock.setLastEntryDate(LocalDateTime.now());

        input.setAveragePurchasePrice(newCMP);
        input.setLastPurchasePrice(event.unitPrice());

        StockMovement movement = new StockMovement();
        movement.setStock(stock);
        movement.setProperty(propertyRepository.getReferenceById(event.propertyId()));
        movement.setUser(userRepository.getReferenceById(event.receivedBy()));
        movement.setQuantity(event.quantity());
        movement.setUnitValue(event.unitPrice());
        movement.setTotalValue(event.totalValue());
        movement.setMovementType(MovementType.PURCHASE);
        movement.setMovementDate(LocalDateTime.now());
        movement.setNotes("Batch received id=" + event.batchId());
        stockMovementRepository.save(movement);

        log.info("Stock updated after batch received id={} property={}",
                event.batchId(), event.propertyId());
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
}
