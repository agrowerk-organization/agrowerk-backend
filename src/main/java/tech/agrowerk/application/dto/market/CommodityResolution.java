package tech.agrowerk.application.dto.market;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CommodityResolution(
        BigDecimal suggestedQuantity,
        BigDecimal referencePrice,
        LocalDate referencePriceDate
) {
}
