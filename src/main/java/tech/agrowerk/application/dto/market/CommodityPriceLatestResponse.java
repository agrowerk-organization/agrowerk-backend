package tech.agrowerk.application.dto.market;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CommodityPriceLatestResponse(
        BigDecimal price,
        LocalDate referenceDate
) {
}
