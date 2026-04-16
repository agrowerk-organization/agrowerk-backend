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

        log.info("Generating Monthly Trend Report: {} to {}", start, end);

        List<CommodityPrice> allPrices = commodityPriceRepository
                .findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                        List.of(Commodity.values()), start, end
                );

        if (allPrices.isEmpty()) {
            return createEmptyReport(start, end);
        }

        BigDecimal avgExchangeRate = MarketAnalysisHelper.calculateAverageExchange(allPrices);
        BigDecimal exchangeVariation = BigDecimal.ZERO;
        if (allPrices.size() >= 2) {
            exchangeVariation = MarketAnalysisHelper.calculateVariation(
                    allPrices.getFirst().getExchangeRate(),
                    allPrices.getLast().getExchangeRate()
            );
        }

        Map<Commodity, List<CommodityPrice>> pricesByComm = allPrices.stream()
                .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        Map<Commodity, BigDecimal> variations = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> highs = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> lows = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> averages = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> medians = new EnumMap<>(Commodity.class);
        List<String> insights = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            BigDecimal first = prices.getFirst().getPrice();
            BigDecimal last = prices.getLast().getPrice();

            BigDecimal variation = MarketAnalysisHelper.calculateVariation(first, last);
            BigDecimal avg = MarketAnalysisHelper.calculateAverage(prices);
            BigDecimal median = MarketAnalysisHelper.calculateMedian(prices);

            String trend = (variation.abs().compareTo(BigDecimal.valueOf(2)) < 0) ? "SIDEWAYS" :
                    (variation.signum() > 0) ? "UPTREND" : "DOWNTREND";

            variations.put(commodity, variation);
            averages.put(commodity, avg);
            medians.put(commodity, median);
            highs.put(commodity, prices.stream().map(CommodityPrice::getPrice).max(BigDecimal::compareTo).orElse(last));
            lows.put(commodity, prices.stream().map(CommodityPrice::getPrice).min(BigDecimal::compareTo).orElse(last));

            insights.add(String.format("%s: %s (%.2f%% no mês | Mediana R$ %.2f)",
                    commodity.name(), trend, variation, median));
        });

        ReportPayload payload = new ReportPayload(
                variations,
                highs,
                lows,
                medians,
                averages,
                avgExchangeRate,
                exchangeVariation,
                insights
        );

        return marketReportRepository.save(MarketReport.builder()
                .reportType(ReportType.MONTHLY_TREND)
                .periodStart(start)
                .periodEnd(end)
                .summary("Desempenho mensal das commodities com foco em liquidez operacional.")
                .reportPayload(payload)
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build());
    }

    private MarketReport createEmptyReport(LocalDate start, LocalDate end) {
        return MarketReport.builder()
                .reportType(ReportType.MONTHLY_TREND)
                .periodStart(start)
                .periodEnd(end)
                .reportStatus(ReportStatus.FAILED)
                .summary("Dados insuficientes para o fechamento mensal.")
                .build();
    }
}