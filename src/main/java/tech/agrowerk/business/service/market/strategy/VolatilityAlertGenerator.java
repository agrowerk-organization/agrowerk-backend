package tech.agrowerk.business.service.market.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.market.*;
import tech.agrowerk.infrastructure.model.market.enums.*;
import tech.agrowerk.infrastructure.repository.market.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VolatilityAlertGenerator implements ReportGenerator {

    private final CommodityPriceRepository commodityPriceRepository;
    private final MarketReportRepository marketReportRepository;

    public VolatilityAlertGenerator(CommodityPriceRepository commodityPriceRepository,
                                    MarketReportRepository marketReportRepository) {
        this.commodityPriceRepository = commodityPriceRepository;
        this.marketReportRepository = marketReportRepository;
    }

    @Override
    public ReportType getType() {
        return ReportType.VOLATILITY_ALERT;
    }

    @Override
    @Transactional
    public MarketReport generate() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(15);

        log.info("Scanning for volatility alerts from {} to {}", start, end);

        Map<Commodity, List<CommodityPrice>> pricesByComm =
                commodityPriceRepository.findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                                List.of(Commodity.values()), start, end)
                        .stream()
                        .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        Map<Commodity, BigDecimal> currentVariations = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> highs = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> lows = new EnumMap<>(Commodity.class);
        List<String> alerts = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            List<BigDecimal> values = prices.stream().map(CommodityPrice::getPrice).toList();
            BigDecimal last = values.getLast();
            BigDecimal first = values.getFirst();

            currentVariations.put(commodity, MarketAnalysisHelper.calculateVariation(first, last));
            highs.put(commodity, values.stream().max(BigDecimal::compareTo).orElse(last));
            lows.put(commodity, values.stream().min(BigDecimal::compareTo).orElse(last));

            BigDecimal mean = MarketAnalysisHelper.calculateAverage(prices);
            BigDecimal variance = values.stream()
                    .map(v -> v.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);

            double stdDev = Math.sqrt(variance.doubleValue());
            BigDecimal diff = last.subtract(mean).abs();

            if (diff.compareTo(BigDecimal.valueOf(stdDev * 2)) > 0) {
                alerts.add(String.format(
                        "ALERTA: %s apresenta volatilidade anormal. Desvio: %.2f | Atual: R$ %.2f",
                        commodity.name(), stdDev, last
                ));
            }
        });

        ReportPayload payload = new ReportPayload(
                currentVariations,
                highs,
                lows,
                BigDecimal.ZERO,
                alerts
        );

        return marketReportRepository.save(MarketReport.builder()
                .reportType(ReportType.VOLATILITY_ALERT)
                .periodStart(start)
                .periodEnd(end)
                .summary(alerts.isEmpty() ? "Mercado operando em estabilidade." : "Sinais de volatilidade detectados.")
                .reportPayload(payload)
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build());
    }
}