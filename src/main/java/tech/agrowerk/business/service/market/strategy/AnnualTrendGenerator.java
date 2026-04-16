package tech.agrowerk.business.service.market.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.market.*;
import tech.agrowerk.infrastructure.model.market.enums.*;
import tech.agrowerk.infrastructure.repository.market.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnnualTrendGenerator implements ReportGenerator {

    private final CommodityPriceRepository commodityPriceRepository;
    private final MarketReportRepository marketReportRepository;

    public AnnualTrendGenerator(CommodityPriceRepository commodityPriceRepository,
                                MarketReportRepository marketReportRepository) {
        this.commodityPriceRepository = commodityPriceRepository;
        this.marketReportRepository = marketReportRepository;
    }

    @Override
    public ReportType getType() {
        return ReportType.ANNUAL_TREND;
    }

    @Override
    @Transactional
    public MarketReport generate() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusYears(1);

        log.info("Generating Annual Trend Report for period: {} to {}", start, end);

        Map<Commodity, List<CommodityPrice>> pricesByComm =
                commodityPriceRepository
                        .findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                                List.of(Commodity.values()), start, end
                        )
                        .stream()
                        .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        Map<Commodity, BigDecimal> priceChanges = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> highestPrices = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> lowestPrices = new EnumMap<>(Commodity.class);
        List<String> insights = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            BigDecimal first = prices.getFirst().getPrice();
            BigDecimal last  = prices.getLast().getPrice();

            BigDecimal variation = MarketAnalysisHelper.calculateVariation(first, last);
            BigDecimal avg = MarketAnalysisHelper.calculateAverage(prices);
            String trend = MarketAnalysisHelper.determineTrend(variation);

            // Extremos
            BigDecimal max = prices.stream().map(CommodityPrice::getPrice).max(BigDecimal::compareTo).orElse(last);
            BigDecimal min = prices.stream().map(CommodityPrice::getPrice).min(BigDecimal::compareTo).orElse(last);

            priceChanges.put(commodity, variation);
            highestPrices.put(commodity, max);
            lowestPrices.put(commodity, min);

            insights.add(String.format("%s: %s (%.2f%%, avg R$ %.2f)",
                    commodity.name(), trend, variation, avg));
        });

        ReportPayload payload = new ReportPayload(
                priceChanges,
                highestPrices,
                lowestPrices,
                BigDecimal.ZERO,
                insights
        );

        return marketReportRepository.save(MarketReport.builder()
                .reportType(ReportType.ANNUAL_TREND)
                .periodStart(start)
                .periodEnd(end)
                .summary("Annual analysis of commodity price movements and market volatility.")
                .reportPayload(payload)
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build());
    }
}