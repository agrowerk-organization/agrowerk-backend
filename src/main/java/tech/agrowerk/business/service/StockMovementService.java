package tech.agrowerk.business.service;

import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.repository.StockRepository;

@Service
public class StockMovementService {
    private final StockRepository stockRepository;

    public StockMovementService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }
}
