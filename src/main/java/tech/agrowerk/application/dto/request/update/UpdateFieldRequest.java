package tech.agrowerk.application.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SoilType;

import java.math.BigDecimal;
import java.util.UUID;

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
