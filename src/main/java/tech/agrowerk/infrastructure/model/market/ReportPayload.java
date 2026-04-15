package tech.agrowerk.infrastructure.model.market;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ReportPayload(
        Map<Commodity, BigDecimal> priceChangePercent,
        Map<Commodity, BigDecimal> highestPrice,
        Map<Commodity, BigDecimal> lowestPrice,
        BigDecimal avgExchangeRate,
        List<String> highlights
) {
}
