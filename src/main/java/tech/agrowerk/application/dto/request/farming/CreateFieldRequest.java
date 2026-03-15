package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SoilType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFieldRequest(
        @NotNull
        UUID propertyId,

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 20)
        String code,

        @NotNull
        @Positive
        BigDecimal areaHectares,

        String description,

        @NotNull
        SoilType soilType,

        @NotNull
        FieldStatus fieldStatus,

        BigDecimal slopePercentage,

        String notes,

        BigDecimal latitude,

        BigDecimal longitude
) {
}
