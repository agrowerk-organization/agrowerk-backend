package tech.agrowerk.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HarvestResponse(
        UUID id,
        UUID plantingId,
        String cropName,
        String cropVarietyName,
        String fieldName,
        String propertyName,
        String seasonName,
        LocalDate harvestDate,
        String qualityGrade,
        BigDecimal totalPlantingCost,
        BigDecimal totalQuantitykg,
        BigDecimal weightedAverageCost,
        Instant createdAt
) {}