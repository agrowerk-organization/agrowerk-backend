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
public class SemiAnnualTrendGenerator implements ReportGenerator {

    private final CommodityPriceRepository commodityPriceRepository;
    private final MarketReportRepository marketReportRepository;

    public SemiAnnualTrendGenerator(CommodityPriceRepository commodityPriceRepository,
                                    MarketReportRepository marketReportRepository) {
        this.commodityPriceRepository = commodityPriceRepository;
        this.marketReportRepository = marketReportRepository;
    }

    @Override
    public ReportType getType() {
        return ReportType.SEMI_ANNUAL_TREND;
    }

    @Override
    @Transactional
    public MarketReport generate() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(180);

        Map<Commodity, List<CommodityPrice>> pricesByComm =
                commodityPriceRepository.findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                                List.of(Commodity.values()), start, end)
                        .stream()
                        .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        Map<Commodity, BigDecimal> variations = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> highs = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> lows = new EnumMap<>(Commodity.class);
        List<String> insights = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            BigDecimal variation = MarketAnalysisHelper.calculateVariation(prices.getFirst().getPrice(), prices.getLast().getPrice());

            variations.put(commodity, variation);
            highs.put(commodity, prices.stream().map(CommodityPrice::getPrice).max(BigDecimal::compareTo).get());
            lows.put(commodity, prices.stream().map(CommodityPrice::getPrice).min(BigDecimal::compareTo).get());

            insights.add(String.format("%s: %s (Variação semestral de %.2f%%)",
                    commodity.name(), MarketAnalysisHelper.determineTrend(variation), variation));
        });

        return marketReportRepository.save(MarketReport.builder()
                .reportType(ReportType.SEMI_ANNUAL_TREND)
                .periodStart(start)
                .periodEnd(end)
                .summary("Relatório semestral de tendências e volatilidade.")
                .reportPayload(new ReportPayload(variations, highs, lows, BigDecimal.ZERO, insights))
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build());
    }
}