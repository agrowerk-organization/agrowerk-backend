package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record HarvestPartialAddedEvent(
        UUID partialId,
        UUID harvestId,
        UUID plantingId,
        UUID propertyId,
        BigDecimal quantityKg,
        String cropName,
        UUID responsibleUserId
) {}
