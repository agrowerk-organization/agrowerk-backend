package tech.agrowerk.application.dto.request.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.shared_enums.ConfidenceLevel;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateHarvestForecastRequest(
        @Positive
        BigDecimal estimatedQuantity,

        @NotNull
        LocalDate forecastDate,

        ConfidenceLevel confidenceLevel,

        @Positive
        BigDecimal plantedArea,

        String notes
) {}