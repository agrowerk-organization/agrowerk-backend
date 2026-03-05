package tech.agrowerk.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FieldResponse(
        UUID id,
        String name,
        String code,
        BigDecimal areaHectares,
        String description,
        String soilType,
        String fieldStatus,
        BigDecimal slopePercentage,
        String notes,
        BigDecimal latitude,
        BigDecimal longitude,
        UUID propertyId,
        String propertyName,
        Instant createdAt,
        Instant updatedAt
) {}