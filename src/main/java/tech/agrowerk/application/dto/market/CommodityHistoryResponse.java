package tech.agrowerk.application.dto.market;

import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.util.List;

public record CommodityHistoryResponse(
        Commodity commodity,
        List<CommodityPriceResponse> prices,
        int totalRecords
) {
}
