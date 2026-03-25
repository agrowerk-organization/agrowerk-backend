package tech.agrowerk.application.dto.request.supplier;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateSupplierRatingRequest(
        @NotNull
        UUID supplierId,

        @NotNull @DecimalMin("0.0") @DecimalMax("5.0")
        BigDecimal rating,

        @Size(max = 1000)
        String comment
) {}