package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreatePlantingRequest(
        @NotNull
        UUID propertyId,

        @NotNull
        UUID fieldId,

        @NotNull
        UUID seasonId,

        @NotNull
        UUID cropVarietyId,

        @NotNull
        @Positive
        BigDecimal areaHectares,

        @NotNull
        LocalDate plantingDate,

        @NotNull
        LocalDate expectedHarvestDate
) {}