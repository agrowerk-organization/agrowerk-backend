package tech.agrowerk.application.dto.response.farming;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PlantingResponse(
        UUID id,
        UUID propertyId,
        String propertyName,
        UUID fieldId,
        String fieldName,
        UUID seasonId,
        String seasonName,
        UUID cropVarietyId,
        String cropVarietyName,
        String cropName,
        BigDecimal areaHectares,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate,
        String plantingStatus,
        Instant createdAt,
        Instant updatedAt
) {}