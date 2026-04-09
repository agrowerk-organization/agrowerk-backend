package tech.agrowerk.application.dto.market;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.util.List;

public record CommoditySeries(
        Commodity commodity,
        String label,
        List<CommodityPriceResponse> data
) {
}
