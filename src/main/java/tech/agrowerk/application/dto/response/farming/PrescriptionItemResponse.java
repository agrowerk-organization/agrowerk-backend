package tech.agrowerk.application.dto.response.farming;

import java.math.BigDecimal;
import java.util.UUID;

public record PrescriptionItemResponse(
        UUID id,
        UUID inputId,
        String inputName,
        BigDecimal authorizedQuantity,
        String unit,
        String usageInstructions
) {
}
