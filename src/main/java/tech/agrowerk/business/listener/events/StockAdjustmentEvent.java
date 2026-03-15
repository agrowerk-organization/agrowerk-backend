package tech.agrowerk.business.listener.events;

import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentEvent(
        UUID stockId,
        UUID propertyId,
        UUID userId,
        BigDecimal quantity,
        MovementType adjustmentType,
        String justification,
        String documentNumber
) {
}
