package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.inventory.StockResponse;
import tech.agrowerk.infrastructure.model.inventory.Stock;

import java.math.BigDecimal;

@Component
public class StockMapper {

    public StockResponse toResponse(Stock stock) {
        String alert = calculateAlert(stock);

        return new StockResponse(
                stock.getId(),
                stock.getProperty().getId(),
                stock.getProperty().getName(),
                stock.getInput() != null
                        ? stock.getInput().getId() : null,
                stock.getInput() != null
                        ? stock.getInput().getName() : null,
                stock.getInput() != null
                        ? stock.getInput().getCategory().getName() : null,
                stock.getStockType().name(),
                stock.getCurrentQuantity(),
                stock.getReservedQuantity(),
                stock.getAvailableQuantity(),
                stock.getTotalValue(),
                stock.getWeightedAverageCost(),
                alert,
                stock.getWarehouse() != null
                        ? stock.getWarehouse().getId() : null,
                stock.getWarehouse() != null
                        ? stock.getWarehouse().getName() : null,
                stock.getLastEntryDate(),
                stock.getLastExitDate(),
                stock.getCreatedAt(),
                stock.getUpdatedAt()
        );
    }

    private String calculateAlert(Stock stock) {
        if (stock.getInput() == null) return "NORMAL";

        BigDecimal current = stock.getCurrentQuantity();
        BigDecimal min = stock.getInput().getMinimumStock();
        BigDecimal max = stock.getInput().getMaximumStock();

        if (min != null && current.compareTo(min) <= 0) return "LOW";
        if (max != null && current.compareTo(max) >= 0) return "HIGH";
        return "NORMAL";
    }
}