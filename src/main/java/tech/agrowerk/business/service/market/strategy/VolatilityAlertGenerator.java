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

        Map<Commodity, List<CommodityPrice>> pricesByComm =
                commodityPriceRepository
                        .findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                                List.of(Commodity.values()), start, end
                        )
                        .stream()
                        .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        List<String> alerts = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            List<BigDecimal> values = prices.stream()
                    .map(CommodityPrice::getPrice)
                    .toList();

            BigDecimal mean = values.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);

            BigDecimal variance = values.stream()
                    .map(v -> v.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);

            double stdDev = Math.sqrt(variance.doubleValue());

            BigDecimal last = values.getLast();

            BigDecimal diff = last.subtract(mean).abs();

            if (diff.compareTo(BigDecimal.valueOf(stdDev * 2)) > 0) {
                alerts.add(String.format(
                        "%s com alta volatilidade (desvio %.2f, preço atual %.2f)",
                        commodity.name(), stdDev, last
                ));
            }
        });

        ReportPayload payload = new ReportPayload(
                null,
                null,
                null,
                null,
                alerts
        );

        MarketReport report = MarketReport.builder()
                .reportType(ReportType.VOLATILITY_ALERT)
                .periodStart(start)
                .periodEnd(end)
                .summary(alerts.isEmpty()
                        ? "Sem sinais relevantes de volatilidade."
                        : "Alertas de volatilidade detectados.")
                .reportPayload(payload)
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build();

        return marketReportRepository.save(report);
    }
}