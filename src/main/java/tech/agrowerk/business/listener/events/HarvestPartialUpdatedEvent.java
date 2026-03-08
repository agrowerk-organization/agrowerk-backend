package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record HarvestPartialUpdatedEvent(
        UUID partialId,
        UUID harvestId,
        UUID propertyId,
        BigDecimal previousQuantityKg,
        BigDecimal newQuantityKg,
        String cropName
) {
}
