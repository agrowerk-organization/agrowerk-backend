package tech.agrowerk.application.dto.response.barter;

import java.math.BigDecimal;
import java.util.UUID;

public record BarterOfferItemResponse(
        UUID id,
        UUID inputId,
        String inputName,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPriceBrl,
        BigDecimal totalPriceBrl,
        String notes
) {}