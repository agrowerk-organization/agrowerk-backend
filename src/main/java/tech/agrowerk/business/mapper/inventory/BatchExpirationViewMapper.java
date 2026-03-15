package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.inventory.BatchExpirationResponse;
import tech.agrowerk.infrastructure.model.inventory.views.BatchExpirationView;

@Component
public class BatchExpirationViewMapper {

    public BatchExpirationResponse toResponse(BatchExpirationView view) {
        return new BatchExpirationResponse(
                view.getBatchId(),
                view.getBatchNumber(),
                view.getInputName(),
                view.getCategoryName(),
                view.getPropertyId(),
                view.getPropertyName(),
                view.getSupplierName(),
                view.getCurrentQuantity(),
                view.getExpirationDate(),
                view.getUnitPrice(),
                view.getCurrentValue(),
                view.getDaysUntilExpiration(),
                view.getExpirationStatus()
        );
    }
}