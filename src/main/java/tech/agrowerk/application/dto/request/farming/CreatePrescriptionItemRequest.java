package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePrescriptionItemRequest(
        @NotNull
        UUID inputId,

        @NotNull
        @Positive
        BigDecimal authorizedQuantity,

        @NotNull
        UnitOfMeasure unit,

        String usageInstructions
) {}