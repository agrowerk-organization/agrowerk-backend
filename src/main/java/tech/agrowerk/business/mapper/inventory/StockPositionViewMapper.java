package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.views.StockPositionResponse;
import tech.agrowerk.infrastructure.model.inventory.views.StockPositionView;

@Component
public class StockPositionViewMapper {

    public StockPositionResponse toResponse(StockPositionView view) {
        return new StockPositionResponse(
                view.getStockId(),
                view.getPropertyId(),
                view.getPropertyName(),
                view.getInputName(),
                view.getCategoryName(),
                view.getStockType(),
                view.getCurrentQuantity(),
                view.getReservedQuantity(),
                view.getAvailableQuantity(),
                view.getWeightedAverageCost(),
                view.getTotalValue(),
                view.getMinimumStock(),
                view.getMaximumStock(),
                view.getStockAlert(),
                view.getWarehouseName(),
                view.getLastEntryDate(),
                view.getLastExitDate()
        );
    }
}