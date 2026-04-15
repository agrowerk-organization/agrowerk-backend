package tech.agrowerk.business.service.market.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;
import tech.agrowerk.infrastructure.model.market.MarketReport;
import tech.agrowerk.infrastructure.model.market.ReportPayload;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.model.market.enums.ReportStatus;
import tech.agrowerk.infrastructure.model.market.enums.ReportType;
import tech.agrowerk.infrastructure.repository.market.CommodityPriceRepository;
import tech.agrowerk.infrastructure.repository.market.ExchangeRateRepository;
import tech.agrowerk.infrastructure.repository.market.MarketReportRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeeklySummaryGenerator implements ReportGenerator {

    private final MarketReportRepository marketReportRepository;
    private final CommodityPriceRepository commodityPriceRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public WeeklySummaryGenerator(MarketReportRepository marketReportRepository,
                                  CommodityPriceRepository commodityPriceRepository,
                                  ExchangeRateRepository exchangeRateRepository) {
        this.marketReportRepository = marketReportRepository;
        this.commodityPriceRepository = commodityPriceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public ReportType getType() {
        return ReportType.WEEKLY_SUMMARY;
    }

    @Override
    @Transactional
    public MarketReport generate() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(7);

        Map<Commodity, List<CommodityPrice>> pricesByComm =
                commodityPriceRepository
                        .findByCommodityInAndReferenceDateBetweenOrderByReferenceDateAsc(
                                List.of(Commodity.values()), start, end
                        )
                        .stream()
                        .collect(Collectors.groupingBy(CommodityPrice::getCommodity));

        Map<Commodity, BigDecimal> changePercent = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> highs = new EnumMap<>(Commodity.class);
        Map<Commodity, BigDecimal> lows = new EnumMap<>(Commodity.class);
        List<String> highlights = new ArrayList<>();

        pricesByComm.forEach((commodity, prices) -> {
            if (prices.size() < 2) return;

            BigDecimal first = prices.getFirst().getPrice();
            BigDecimal last = prices.getLast().getPrice();

            BigDecimal change = last.subtract(first)
                    .divide(first, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            changePercent.put(commodity, change);

            highs.put(commodity, prices.stream()
                    .map(CommodityPrice::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(last));

            lows.put(commodity, prices.stream()
                    .map(CommodityPrice::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(last));

            if (change.abs().compareTo(BigDecimal.valueOf(3)) > 0) {
                highlights.add(String.format("%s variou %.2f%% na semana", commodity.name(), change));
            }
        });

        ReportPayload payload = new ReportPayload(
                changePercent,
                highs,
                lows,
                calcAvgExchangeRate(start, end),
                highlights
        );

        MarketReport report = MarketReport.builder()
                .reportType(ReportType.WEEKLY_SUMMARY)
                .periodStart(start)
                .periodEnd(end)
                .summary(buildSummaryText(changePercent, highlights))
                .reportPayload(payload)
                .generatedAt(LocalDateTime.now())
                .reportStatus(ReportStatus.GENERATED)
                .build();

        return marketReportRepository.save(report);
    }

    private BigDecimal calcAvgExchangeRate(LocalDate start, LocalDate end) {
        List<BigDecimal> rates = exchangeRateRepository
                .findByCurrencyPairAndReferenceDateBetweenOrderByReferenceDateAsc(
                        "USD_BRL", start, end
                )
                .stream()
                .map(ExchangeRate::getRate)
                .toList();

        if (rates.isEmpty()) return BigDecimal.ZERO;

        return rates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 4, RoundingMode.HALF_UP);
    }

    private String buildSummaryText(Map<Commodity, BigDecimal> changePercent,
                                    List<String> highlights) {

        if (changePercent.isEmpty()) {
            return "Sem dados suficientes para gerar resumo semanal.";
        }

        Optional<Map.Entry<Commodity, BigDecimal>> biggest =
                changePercent.entrySet()
                        .stream()
                        .max(Comparator.comparing(e -> e.getValue().abs()));

        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Resumo semanal de %s a %s. ",
                LocalDate.now().minusDays(7), LocalDate.now()));

        biggest.ifPresent(e ->
                sb.append(String.format("Maior movimentação: %s com %.2f%%. ",
                        e.getKey().name(), e.getValue()))
        );

        if (!highlights.isEmpty()) {
            sb.append("Destaques: ")
                    .append(String.join("; ", highlights))
                    .append(".");
        }

        return sb.toString();
    }
}
