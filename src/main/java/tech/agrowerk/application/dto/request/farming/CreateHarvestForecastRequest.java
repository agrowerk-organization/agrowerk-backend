package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.shared_enums.ConfidenceLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateHarvestForecastRequest(
        @NotNull
        UUID plantingId,

        @NotNull
        @Positive
        BigDecimal estimatedQuantity,

        @NotNull
        LocalDate forecastDate,

        @NotNull
        ConfidenceLevel confidenceLevel,

        @Positive
        BigDecimal plantedArea,

        String notes
) {}