package tech.agrowerk.application.dto.market;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketPrice(
        Commodity commodity,
        BigDecimal priceUsd,
        BigDecimal priceBrl,
        BigDecimal exchangeRate,
        String unit,
        String source,
        LocalDate referenceDate
) {
}
