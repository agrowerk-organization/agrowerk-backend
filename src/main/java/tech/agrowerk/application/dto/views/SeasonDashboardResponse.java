package tech.agrowerk.application.dto.views;

import java.math.BigDecimal;
import java.util.UUID;

public record SeasonDashboardResponse(
        UUID seasonId,
        String seasonName,
        UUID propertyId,
        String propertyName,
        String cropName,
        Long totalPlantings,
        BigDecimal totalArea,
        BigDecimal totalProducedKg,
        BigDecimal avgProductivity,
        String warning
) {}
