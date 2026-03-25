package tech.agrowerk.infrastructure.client;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tech.agrowerk.application.dto.market.MarketPrice;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@Slf4j
public class MarketDataClient {

    private static final String BASE_URL = "https://query1.finance.yahoo.com";
    private static final String CHART_PATH = "/v8/finance/chart/{symbol}";
    private static final String FX_SYMBOL = "BRL=X";

    private static final Map<Commodity, String> SYMBOLS = Map.of(
            Commodity.SOJA, "ZS=F",
            Commodity.MILHO, "ZC=F",
            Commodity.CAFE, "KC=F",
            Commodity.BOI_GORDO, "GF=F",
            Commodity.TRIGO, "ZW=F",
            Commodity.ALGODAO, "CT=F"
    );

    private static final Map<Commodity, String> UNITS = Map.of(
            Commodity.SOJA,      "R$/saca",
            Commodity.MILHO,     "R$/saca",
            Commodity.CAFE,      "R$/saca",
            Commodity.BOI_GORDO, "R$/arroba",
            Commodity.TRIGO,     "R$/saca",
            Commodity.ALGODAO,   "R$/arroba"
    );

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

    public MarketDataClient(RestClient.Builder builder,
                            CircuitBreakerRegistry circuitBreakerRegistry,
                            RetryRegistry retryRegistry,
                            TimeLimiterRegistry timeLimiterRegistry) {

        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new MarketDataException("Yahoo Finance client error: " + res.getStatusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new MarketDataException("Yahoo Finance server error: " + res.getStatusCode());
                })
                .build();

        this.circuitBreaker = (CircuitBreaker) circuitBreakerRegistry.circuitBreaker("marketDataCircuitBreaker");
        this.retry          = (Retry) retryRegistry.retry("marketDataRetry");
        this.timeLimiter    = timeLimiterRegistry.timeLimiter("marketDataTimeLimiter");
    }


    public List<MarketPrice> fetchAll() {
        BigDecimal exchangeRate = fetchExchangeRate();
        log.info("Exchange rate USD/BRL: {}", exchangeRate);

        return SYMBOLS.entrySet().stream()
                .map(entry -> fetchCommodity(entry.getKey(), entry.getValue(), exchangeRate))
                .toList();
    }

    public MarketPrice fetchCommodity(Commodity commodity) {
        BigDecimal exchangeRate = fetchExchangeRate();
        return fetchCommodity(commodity, SYMBOLS.get(commodity), exchangeRate);
    }

    private BigDecimal fetchExchangeRate() {
        YahooFinanceResponse response = fetchWithResilience(FX_SYMBOL);
        YahooFinanceResponse.Meta meta = response.getMeta();

        if (meta == null || meta.regularMarketPrice() == null) {
            throw new MarketApiException("Could not fetch USD/BRL exchange rate");
        }

        return meta.regularMarketPrice().setScale(4, RoundingMode.HALF_UP);
    }


    private MarketPrice fetchCommodity(Commodity commodity, String symbol, BigDecimal exchangeRate) {
        log.debug("Fetching {} ({})", commodity, symbol);

        YahooFinanceResponse response = fetchWithResilience(symbol);
        YahooFinanceResponse.Meta meta = response.getMeta();

        if (meta == null || meta.regularMarketPrice() == null) {
            throw new MarketDataException("No data returned for symbol: " + symbol);
        }

        BigDecimal rawPrice = meta.regularMarketPrice();
        BigDecimal priceUsd = convertToUsd(commodity, rawPrice);
        BigDecimal priceBrl = convertToBrl(commodity, priceUsd, exchangeRate);

        LocalDate referenceDate = meta.regularMarketTime() != null
                ? Instant.ofEpochSecond(meta.regularMarketTime()).atZone(ZoneId.of("America/Sao_Paulo")).toLocalDate()
                : LocalDate.now();

        log.info("{}: raw={} {} | USD={} | BRL={} {} | câmbio={}",
                commodity, rawPrice, meta.currency(), priceUsd, priceBrl,
                UNITS.get(commodity), exchangeRate);

        return new MarketPrice(
                commodity, priceUsd, priceBrl, exchangeRate,
                UNITS.get(commodity), "YAHOO_FINANCE", referenceDate
        );
    }

    private BigDecimal convertToUsd(Commodity commodity, BigDecimal rawPrice) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);

        return switch (commodity) {
            case SOJA, TRIGO ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(BigDecimal.valueOf(60), mc)
                            .divide(BigDecimal.valueOf(27.2155), mc)
                            .setScale(4, RoundingMode.HALF_UP);

            case MILHO ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(BigDecimal.valueOf(60), mc)
                            .divide(BigDecimal.valueOf(25.4012), mc)
                            .setScale(4, RoundingMode.HALF_UP);

            case CAFE ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(BigDecimal.valueOf(132.277), mc)
                            .setScale(4, RoundingMode.HALF_UP);

            case ALGODAO ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(BigDecimal.valueOf(33.069), mc)
                            .setScale(4, RoundingMode.HALF_UP);

            case BOI_GORDO ->
                    rawPrice.divide(BigDecimal.valueOf(45.3592), mc)
                            .multiply(BigDecimal.valueOf(15), mc)
                            .setScale(4, RoundingMode.HALF_UP);
        };
    }

    private BigDecimal convertToBrl(Commodity commodity, BigDecimal priceUsd, BigDecimal exchangeRate) {
        return priceUsd.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }


    private YahooFinanceResponse fetchWithResilience(String symbol) {
        try {
            return Decorators
                    .ofCompletionStage(() -> CompletableFuture.supplyAsync(() -> makeRequest(symbol)))
                    .withRetry(retry, scheduler)
                    .withCircuitBreaker(circuitBreaker)
                    .withTimeLimiter(timeLimiter, scheduler)
                    .get()
                    .toCompletableFuture()
                    .join();
        } catch (Exception e) {
            log.error("Failed to fetch symbol {}: {}", symbol, e.getMessage());
            throw new MarketDataException("Market data unavailable for " + symbol, e);
        }
    }

    private YahooFinanceResponse makeRequest(String symbol) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(CHART_PATH)
                        .queryParam("interval", "1d")
                        .queryParam("range", "1d")
                        .build(symbol))
                .retrieve()
                .body(YahooFinanceResponse.class);
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }
}