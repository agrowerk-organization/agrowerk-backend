package tech.agrowerk.application.dto.market;

import java.util.List;

public record CommodityDashboardResponse(
        List<CommodityPriceResponse> latestPrices,
        List<CommodityPriceResponse> sojaHistory,
        List<CommodityPriceResponse> milhoHistory,
        List<CommodityPriceResponse> feijaoHistory
) {
}
