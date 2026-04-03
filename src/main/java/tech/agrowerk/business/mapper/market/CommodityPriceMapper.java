package tech.agrowerk.business.mapper.market;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.market.CommodityPriceResponse;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CommodityPriceMapper {

    public CommodityPriceResponse toResponse(CommodityPrice price, CommodityPrice previous) {
        BigDecimal variation = calculateVariation(price.getPrice(),
                previous != null ? previous.getPrice() : null);

        return new CommodityPriceResponse(
                price.getCommodity(),
                price.getPrice(),
                price.getUnit(),
                price.getReferenceDate(),
                variation
        );
    }

    public CommodityPriceResponse toResponse(CommodityPrice price) {
        return toResponse(price, null);
    }

    private BigDecimal calculateVariation(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) return null;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}