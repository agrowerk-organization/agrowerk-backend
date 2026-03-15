package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.inventory.StockMovementResponse;
import tech.agrowerk.infrastructure.model.inventory.views.StockMovementView;

@Component
public class StockMovementViewMapper {

    public StockMovementResponse toResponse(StockMovementView view) {
        return new StockMovementResponse(
                view.getMovementId(),
                view.getMovementType(),
                view.getQuantity(),
                view.getUnitValue(),
                view.getTotalValue(),
                view.getMovementDate(),
                view.getPropertyId(),
                view.getPropertyName(),
                view.getInputName(),
                view.getUserName(),
                view.getBatchNumber(),
                view.getNotes(),
                view.getReversed(),
                view.getReversedMovementId()
        );
    }
}