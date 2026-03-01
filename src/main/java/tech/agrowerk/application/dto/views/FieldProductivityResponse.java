package tech.agrowerk.application.dto.views;

import java.math.BigDecimal;
import java.util.UUID;

public record FieldProductivityResponse(
        UUID fieldId,
        String fieldName,
        String propertyName,
        BigDecimal avgProductivity,
        BigDecimal totalProducedKg,
        Long totalHarvests
) {
}
