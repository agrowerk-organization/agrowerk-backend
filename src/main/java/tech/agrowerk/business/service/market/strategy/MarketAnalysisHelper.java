package tech.agrowerk.business.service.market.strategy;

import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class MarketAnalysisHelper {

    public static BigDecimal calculateVariation(BigDecimal first, BigDecimal last) {
        if (first.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return last.subtract(first)
                .divide(first, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateAverage(List<CommodityPrice> prices) {
       if (prices == null || prices.isEmpty()) return BigDecimal.ZERO;

        return prices.stream()
                .map(CommodityPrice::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateMedian(List<CommodityPrice> prices) {
        if (prices == null || prices.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> sortedPrices = prices.stream()
                .map(CommodityPrice::getPrice)
                .sorted()
                .toList();

        int size = sortedPrices.size();

        if (size % 2 == 0) {
            return sortedPrices.get(size / 2);
        } else {
            BigDecimal mid1 = sortedPrices.get(size / 2 - 1);
            BigDecimal mid2 = sortedPrices.get(size / 2);
            return mid1.add(mid2).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
    }

    public static BigDecimal calculateAverageExchange(List<CommodityPrice> allPrices) {
        if (allPrices == null || allPrices.isEmpty()) return BigDecimal.ZERO;
        return allPrices.stream()
                .map(CommodityPrice::getExchangeRate)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allPrices.size()), 4, RoundingMode.HALF_UP);
    }

    public static String determineTrend(BigDecimal variation) {
        if (variation.compareTo(BigDecimal.valueOf(10)) > 0) return "UPTREND";
        if (variation.compareTo(BigDecimal.valueOf(-10)) < 0) return "DOWNTREND";
        return "SIDEWAYS";
    }
}