package tech.agrowerk.business.service.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.market.CommodityPrice;
import tech.agrowerk.infrastructure.model.market.ExchangeRate;
import tech.agrowerk.infrastructure.model.market.MarketAlert;
import tech.agrowerk.infrastructure.model.market.enums.AlertType;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;
import tech.agrowerk.infrastructure.repository.market.CommodityPriceRepository;
import tech.agrowerk.infrastructure.repository.market.ExchangeRateRepository;
import tech.agrowerk.infrastructure.repository.market.MarketAlertRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MarketAlertService {

    private static final BigDecimal SPIKE_THRESHOLD    = new BigDecimal("4.0");
    private static final BigDecimal DROP_THRESHOLD     = new BigDecimal("-4.0");
    private static final BigDecimal VOLATILITY_THRESHOLD = new BigDecimal("3.0");
    private static final BigDecimal EXCHANGE_SURGE_THRESHOLD = new BigDecimal("2.5");
    private static final int EVALUATION_WINDOW_DAYS   = 7;
    private static final int SELL_WINDOW_LOOKBACK_DAYS = 90;

    private final MarketAlertRepository marketAlertRepository;
    private final CommodityPriceRepository commodityPriceRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public MarketAlertService(MarketAlertRepository marketAlertRepository,
                              CommodityPriceRepository commodityPriceRepository,
                              ExchangeRateRepository exchangeRateRepository) {
        this.marketAlertRepository = marketAlertRepository;
        this.commodityPriceRepository = commodityPriceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }


    @Transactional
    public void evaluateAll() {
        log.info("Starting market alert evaluation");
        LocalDate today = LocalDate.now();

        for (Commodity commodity : Commodity.values()) {
            evaluatePriceAlerts(commodity, today);
            evaluateSellWindow(commodity, today);
        }

        evaluateExchangeSurge(today);
        log.info("Market alert evaluation complete");
    }

    @Transactional(readOnly = true)
    public List<MarketAlert> getUnread() {
        return marketAlertRepository.findByReadFalseOrderByCreatedAtDesc();
    }

    @Transactional
    public void markAsRead(UUID id) {
        marketAlertRepository.findById(id).ifPresent(alert -> {
            alert.markRead();
            marketAlertRepository.save(alert);
        });
    }

    @Transactional
    public void markAllAsRead() {
        marketAlertRepository.findByReadFalseOrderByCreatedAtDesc()
                .forEach(alert -> {
                    alert.markRead();
                    marketAlertRepository.save(alert);
                });
    }

    @Transactional(readOnly = true)
    public long countUnread() {
        return marketAlertRepository.countByReadFalse();
    }


    private void evaluatePriceAlerts(Commodity commodity, LocalDate today) {
        List<CommodityPrice> window = commodityPriceRepository
                .findByCommodityAndReferenceDateBetweenOrderByReferenceDateAsc(
                        commodity, today.minusDays(EVALUATION_WINDOW_DAYS), today);

        if (window.size() < 2) return;

        BigDecimal first = window.getFirst().getPrice();
        BigDecimal last  = window.getLast().getPrice();
        BigDecimal changePercent = calcChangePercent(first, last);

        if (changePercent.compareTo(SPIKE_THRESHOLD) > 0) {
            saveIfAbsent(commodity, AlertType.PRICE_SPIKE, changePercent, today,
                    String.format("%s subiu %.1f%% nos últimos %d dias.",
                            commodity.getDescription(), changePercent, EVALUATION_WINDOW_DAYS));
        }

        if (changePercent.compareTo(DROP_THRESHOLD) < 0) {
            saveIfAbsent(commodity, AlertType.PRICE_DROP, changePercent, today,
                    String.format("%s caiu %.1f%% nos últimos %d dias.",
                            commodity.getDescription(), changePercent.abs(), EVALUATION_WINDOW_DAYS));
        }

        evaluateVolatility(commodity, window, today);
    }


    private void evaluateVolatility(Commodity commodity,
                                    List<CommodityPrice> window,
                                    LocalDate today) {
        if (window.size() < 3) return;

        List<BigDecimal> prices = window.stream()
                .map(CommodityPrice::getPrice)
                .toList();

        BigDecimal mean = prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = prices.stream()
                .map(p -> p.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), 4, RoundingMode.HALF_UP);

        double stdDev = Math.sqrt(variance.doubleValue());

        BigDecimal coefficientOfVariation = BigDecimal.valueOf(stdDev)
                .divide(mean, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        if (coefficientOfVariation.compareTo(VOLATILITY_THRESHOLD) > 0) {
            saveIfAbsent(commodity, AlertType.HIGH_VOLATILITY, coefficientOfVariation, today,
                    String.format("%s apresenta alta volatilidade (CV: %.1f%%) nos últimos %d dias.",
                            commodity.getDescription(), coefficientOfVariation, EVALUATION_WINDOW_DAYS));
        }
    }

    private void evaluateSellWindow(Commodity commodity, LocalDate today) {
        List<CommodityPrice> historical = commodityPriceRepository
                .findByCommodityAndReferenceDateBetweenOrderByReferenceDateAsc(
                        commodity, today.minusDays(SELL_WINDOW_LOOKBACK_DAYS), today);

        if (historical.size() < 10) return;

        BigDecimal avg = historical.stream()
                .map(CommodityPrice::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historical.size()), 4, RoundingMode.HALF_UP);

        BigDecimal current = historical.getLast().getPrice();
        BigDecimal aboveAvgPercent = calcChangePercent(avg, current);

        if (aboveAvgPercent.compareTo(new BigDecimal("5.0")) > 0) {
            saveIfAbsent(commodity, AlertType.SELL_WINDOW, aboveAvgPercent, today,
                    String.format("%s está %.1f%% acima da média dos últimos %d dias — possível janela de venda.",
                            commodity.getDescription(), aboveAvgPercent, SELL_WINDOW_LOOKBACK_DAYS));
        }
    }


    private void evaluateExchangeSurge(LocalDate today) {
        List<ExchangeRate> last7 = exchangeRateRepository
                .findByCurrencyPairAndReferenceDateBetweenOrderByReferenceDateAsc(
                        "USD_BRL", today.minusDays(7), today);

        if (last7.size() < 2) return;

        BigDecimal change = calcChangePercent(
                last7.getFirst().getRate(), last7.getLast().getRate());

        if (change.compareTo(EXCHANGE_SURGE_THRESHOLD) > 0) {
            saveIfAbsent(null, AlertType.EXCHANGE_SURGE, change, today,
                    String.format("Dólar subiu %.1f%% na semana. Insumos importados podem encarecer.", change));
        }

        if (change.compareTo(EXCHANGE_SURGE_THRESHOLD.negate()) < 0) {
            saveIfAbsent(Commodity.GENERAL, AlertType.EXCHANGE_DROP, change.abs(), today,
                    String.format("Dólar caiu %.1f%% na semana. Possível janela para compra de insumos importados.", change.abs()));
        }
    }


    private void saveIfAbsent(Commodity commodity, AlertType type,
                              BigDecimal triggerValue, LocalDate date,
                              String message) {
        if (marketAlertRepository.existsByCommodityAndTypeAndReferenceDate(
                commodity, type, date)) {
            log.debug("Alert already exists: {} {} {}", commodity, type, date);
            return;
        }

        marketAlertRepository.save(MarketAlert.builder()
                .commodity(commodity)
                .type(type)
                .message(message)
                .triggerValue(triggerValue)
                .referenceDate(date)
                .read(false)
                .createdAt(Instant.now())
                .build());

        log.info("Alert saved: {} {} - {}", commodity, type, message);
    }

    private BigDecimal calcChangePercent(BigDecimal from, BigDecimal to) {
        if (from.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return to.subtract(from)
                .divide(from, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}