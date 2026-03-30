package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.PracticeType;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAgriculturalPracticeRequest(
        @NotNull
        UUID plantingId,

        @NotNull
        PracticeType practipeType,

        @NotNull
        LocalDate applicationDate,

        @Size(max = 200)
        String productUsed,

        @Positive
        BigDecimal quantityUsed,

        UnitOfMeasure unitOfMeasure,

        @Positive
        BigDecimal costAmount,

        String observations
) {}