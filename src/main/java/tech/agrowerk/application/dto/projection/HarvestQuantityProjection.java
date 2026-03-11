package tech.agrowerk.application.dto.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record HarvestQuantityProjection(
        UUID harvestId,
        BigDecimal totalQuantityKg
) {
    public HarvestQuantityProjection(UUID harvestId, Number totalQuantityKg) {
        this(harvestId, totalQuantityKg != null ? BigDecimal.valueOf(totalQuantityKg.doubleValue()) : BigDecimal.ZERO);
    }
}
