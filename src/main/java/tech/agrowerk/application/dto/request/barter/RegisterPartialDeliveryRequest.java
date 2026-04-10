package tech.agrowerk.application.dto.request.barter;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterPartialDeliveryRequest(
        @NotNull
        UUID commitmentId,

        @NotNull @Positive
        BigDecimal deliveredQuantity,

        @NotNull
        LocalDate deliveryDate,

        @DecimalMin("0") @DecimalMax("100")
        BigDecimal moisturePercentage,

        @DecimalMin("0") @DecimalMax("100")
        BigDecimal impurityPercentage,

        @Size(max = 50)
        String qualityGrade,

        String notes
) {}