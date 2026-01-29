package tech.agrowerk.business.service.inventory;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.inventory.StockRepository;

@Service
public class StockService {
    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
}
