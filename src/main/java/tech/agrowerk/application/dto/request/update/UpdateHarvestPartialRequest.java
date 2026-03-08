package tech.agrowerk.application.dto.request.update;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateHarvestPartialRequest(
        @Positive
        BigDecimal quantityKg,
        String qualityGrade,
        String notes
) {}