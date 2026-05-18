package tech.agrowerk.application.dto.views;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record HarvestDashboardResponse(
        UUID plantingId,
        UUID propertyId,
        Long totalPartials,
        BigDecimal totalHarvestedKg,
        String qualityGrade,
        Boolean finalized,
        LocalDate harvestDate,
        BigDecimal estimatedQuantity,
        BigDecimal commitedQuantity,
        String confidenceLevel,
        String varietyName,
        String cropName,
        String fieldName,
        String seasonName,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate,
        BigDecimal achievementRate,
        BigDecimal availableQuantity
) {
}
