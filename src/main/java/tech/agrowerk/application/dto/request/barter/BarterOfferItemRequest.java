package tech.agrowerk.application.dto.request.barter;

import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record BarterOfferItemRequest(
        UUID inputId,
        BigDecimal quantity,
        UnitOfMeasure unitOfMeasure,
        BigDecimal unitPriceBrl,
        String notes
) {
}
