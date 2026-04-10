package tech.agrowerk.application.dto.response.barter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PartialDeliveryResponse(
        UUID id,
        UUID commitmentId,
        BigDecimal deliveredQuantity,
        LocalDate deliveryDate,
        BigDecimal moisturePercentage,
        BigDecimal impurityPercentage,
        String qualityGrade,
        String notes
) {
}
