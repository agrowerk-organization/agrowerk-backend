package tech.agrowerk.business.service.market.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.ReportPayload;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.model.market.enums.ReportStatus;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;
import tech.agrowerk.infrastructure.repository.market.CommodityPriceRepository;
import tech.agrowerk.infrastructure.repository.market.MarketReportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MonthlyTrendGenerator implements ReportGenerator {

    private final CommodityPriceRepository commodityPriceRepository;
    private final MarketReportRepository marketReportRepository;

    public MonthlyTrendGenerator(CommodityPriceRepository commodityPriceRepository,
                                 MarketReportRepository marketReportRepository) {
        this.commodityPriceRepository = commodityPriceRepository;
        this.marketReportRepository = marketReportRepository;
    }

    @Override
    public ReportType getType() {
        return ReportType.MONTHLY_TREND;
    }

    @Override
    @Transactional
    public MarketReport generate() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);

        Map<Commodity, List<CommodityPrice>> pricesByComm =
                commodityPriceRepository
                        .findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                                List.of(Commodity.values()), start, end
                        )
                        .stream()
                        .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        Map<Commodity, String> trends = new EnumMap<>(Commodity.class);
        List<String> insights = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            BigDecimal first = prices.getFirst().getPrice();
            BigDecimal last = prices.getLast().getPrice();

            BigDecimal avg = prices.stream()
                    .map(CommodityPrice::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(prices.size()), 4, RoundingMode.HALF_UP);

            BigDecimal variation = last.subtract(first)
                    .divide(first, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            String trend;

            if (variation.compareTo(BigDecimal.valueOf(2)) > 0) {
                trend = "UPTREND";
            } else if (variation.compareTo(BigDecimal.valueOf(-2)) < 0) {
                trend = "DOWNTREND";
            } else {
                trend = "SIDEWAYS";
            }

            trends.put(commodity, trend);

            insights.add(String.format(
                    "%s: %s (%.2f%%, média %.2f)",
                    commodity.name(), trend, variation, avg
            ));
        });

        ReportPayload payload = new ReportPayload(
                null,
                null,
                null,
                null,
                insights
        );

        MarketReport report = MarketReport.builder()
                .reportType(ReportType.MONTHLY_TREND)
                .periodStart(start)
                .periodEnd(end)
                .summary("Análise de tendência mensal das commodities.")
                .reportPayload(payload)
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build();

        return marketReportRepository.save(report);
    }
}