package tech.agrowerk.business.service.market.strategy;

import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MarketAnalysisHelper {

    public static BigDecimal calculateVariation(BigDecimal first, BigDecimal last) {
        if (first.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return last.subtract(first)
                .divide(first, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateAverage(List<CommodityPrice> prices) {
        return prices.stream()
                .map(CommodityPrice::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
    }

    public static String determineTrend(BigDecimal variation) {
        if (variation.compareTo(BigDecimal.valueOf(10)) > 0) return "UPTREND";
        if (variation.compareTo(BigDecimal.valueOf(-10)) < 0) return "DOWNTREND";
        return "SIDEWAYS";
    }
}