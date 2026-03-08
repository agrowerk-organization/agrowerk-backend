package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateHarvestPartialRequest(
        @NotNull
        LocalDate partialDate,
        BigDecimal quantityKg,
        String qualityGrade,
        String notes
) {
}
