package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SoilType;

import java.math.BigDecimal;

public record UpdateFieldRequest(
        @Size(max = 100)
        String name,
        @Size(max = 20)
        String code,
        String description,
        SoilType soilType,
        FieldStatus fieldStatus,
        BigDecimal slopePercentage,
        String notes,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
