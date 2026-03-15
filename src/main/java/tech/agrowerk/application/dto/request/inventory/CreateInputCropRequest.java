package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInputCropRequest(
        @NotNull
        UUID inputId,

        @NotNull
        UUID cropId,

        @NotNull
        String usageRecommendation,

        @Positive
        BigDecimal recommendedDosePerHectare,

        UnitOfMeasure unitOfMeasure
) {
}
