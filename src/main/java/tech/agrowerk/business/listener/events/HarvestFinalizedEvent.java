package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record HarvestFinalizedEvent(
        UUID harvestId,
        UUID plantingId,
        UUID propertyId,
        UUID fieldId,
        BigDecimal areaHectares,
        BigDecimal totalQuantityKg,
        BigDecimal totalPlantingCost,
        String cropName,
        UUID responsibleUserId
) {}