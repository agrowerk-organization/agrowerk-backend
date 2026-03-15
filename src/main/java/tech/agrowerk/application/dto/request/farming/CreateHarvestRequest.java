package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateHarvestRequest(
        @NotNull
        UUID plantingId,

        @NotNull
        LocalDate harvestDate,

        @NotNull
        @Positive
        BigDecimal quantityKg,

        String qualityGrade
) {}