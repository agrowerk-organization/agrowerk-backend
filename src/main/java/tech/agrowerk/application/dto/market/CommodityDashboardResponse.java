package tech.agrowerk.application.dto.market;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CommodityDashboardResponse(
        BigDecimal avgExchangeRate,
        List<CommodityPriceResponse> latestPrices,
        Map<Commodity, List<CommodityPriceResponse>> history
) {
}
