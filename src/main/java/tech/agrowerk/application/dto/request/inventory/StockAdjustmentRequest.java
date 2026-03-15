package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.inventory.enums.MovementType;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentRequest(
        @NotNull
        UUID stockId,

        @NotNull
        @Positive
        BigDecimal quantity,

        @NotNull
        MovementType adjustmentType,

        @NotBlank
        String justification,

        String documentNumber
) {
}
