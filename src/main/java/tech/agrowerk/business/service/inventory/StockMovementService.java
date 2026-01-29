package tech.agrowerk.business.service.inventory;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;

@Service
public class StockMovementService {
    private final StockRepository stockRepository;

    public StockMovementService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
}
