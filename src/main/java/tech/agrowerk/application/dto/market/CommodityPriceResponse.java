package tech.agrowerk.application.dto.market;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CommodityPriceResponse(
        Commodity commodity,
        BigDecimal price,
        String unit,
        String region,
        LocalDate referenceDate,
        BigDecimal variationPercent
) {
}
