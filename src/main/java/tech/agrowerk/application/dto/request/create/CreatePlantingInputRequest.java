package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePlantingInputRequest(
        @NotNull
        UUID plantingId,

        @NotNull
        UUID inputId,

        @NotNull
        UnitOfMeasure unitOfMeasure,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotNull
        LocalDate applicationDate
) {}