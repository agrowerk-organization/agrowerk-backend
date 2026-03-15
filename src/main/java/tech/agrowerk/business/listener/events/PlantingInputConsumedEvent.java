package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PlantingInputConsumedEvent(
        UUID plantingId,
        UUID inputId,
        UUID propertyId,
        UUID userId,
        UUID batchId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String cropName
) {
}
