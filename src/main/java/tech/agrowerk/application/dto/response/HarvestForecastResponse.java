package tech.agrowerk.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HarvestForecastResponse(
        UUID id,
        UUID plantingId,
        String cropName,
        String cropVarietyName,
        String fieldName,
        String seasonName,
        String propertyName,
        BigDecimal estimatedQuantity,
        LocalDate forecastDate,
        String confidenceLevel,
        BigDecimal plantedArea,
        String notes,
        BigDecimal actualQuantityKg,
        BigDecimal forecastAccuracy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}