package tech.agrowerk.infrastructure.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tech.agrowerk.application.dto.market.FinanceResponse;
import tech.agrowerk.application.dto.market.MarketPrice;
import tech.agrowerk.business.service.market.ExchangeRateService;
import tech.agrowerk.infrastructure.exception.local.MarketDataException;
import tech.agrowerk.infrastructure.model.market.enums.Commodity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@Slf4j
public class MarketDataClient {

    private static final String BASE_URL  = "https://www.alphavantage.co";
    private static final String QUERY_PATH = "/query";

    private static final BigDecimal SACAS_POR_TON   = BigDecimal.valueOf(16.6667);
    private static final BigDecimal ARROBAS_POR_TON = BigDecimal.valueOf(66.6667);
    private static final BigDecimal LBS_POR_SACA   = BigDecimal.valueOf(132.277);
    private static final BigDecimal LBS_POR_ARROBA = BigDecimal.valueOf(33.069);
    private static final BigDecimal LBS_POR_KG = BigDecimal.valueOf(2.20462);
    private static final BigDecimal SACAS_POR_BUSHEL_SOJA_TRIGO = BigDecimal.valueOf(2.2046);
    private static final BigDecimal SACAS_POR_BUSHEL_MILHO = BigDecimal.valueOf(2.3622);

    private static final Map<Commodity, String> UNITS = Map.of(
            Commodity.SOJA,    "R$/saca",
            Commodity.MILHO,   "R$/saca",
            Commodity.CAFE,    "R$/saca",
            Commodity.TRIGO,   "R$/saca",
            Commodity.ALGODAO, "R$/arroba",
            Commodity.ACUCAR, "R$/saca"
    );

    @Value("${alphavantage.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ExchangeRateService exchangeRateService;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;
    private final RateLimiter rateLimiter;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public MarketDataClient(RestClient.Builder builder,
                            ExchangeRateService exchangeRateService,
                            CircuitBreakerRegistry circuitBreakerRegistry,
                            RetryRegistry retryRegistry,
                            TimeLimiterRegistry timeLimiterRegistry,
                            RateLimiterRegistry rateLimiterRegistry) {

        this.restClient = builder
                .baseUrl(BASE_URL)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new MarketDataException("Alpha Vantage client error: " + res.getStatusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new MarketDataException("Alpha Vantage server error: " + res.getStatusCode());
                })
                .build();

        this.exchangeRateService = exchangeRateService;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("marketDataCircuitBreaker");
        this.retry          = retryRegistry.retry("marketDataRetry");
        this.timeLimiter    = timeLimiterRegistry.timeLimiter("marketDataTimeLimiter");
        this.rateLimiter = rateLimiterRegistry.rateLimiter("marketDataRateLimiter");
    }

    public List<MarketPrice> fetchAll() {
        BigDecimal exchangeRate = exchangeRateService.getUsdToBrl();
        log.info("Exchange rate USD/BRL: {}", exchangeRate);

        return Arrays.stream(Commodity.values())
                .filter(Commodity::hasAlphaVantageSource)
                .map(commodity ->
                     fetchCommodity(commodity, exchangeRate))
                .flatMap(List::stream)
                .toList();
    }

    /*public MarketPrice fetchCommodity(Commodity commodity) {
        BigDecimal exchangeRate = exchangeRateService.getUsdToBrl();
        return fetchCommodity(commodity, exchangeRate)
                .orElseThrow(() -> new MarketDataException("No data for " + commodity));
    } */

    private List<MarketPrice> fetchCommodity(Commodity commodity, BigDecimal exchangeRate) {
        log.debug("Fetching {} ({})", commodity, commodity.getAlphaVantageFunction());

        try {
            FinanceResponse response = fetchWithResilience(commodity.getAlphaVantageFunction());

            if (response == null || response.data() == null || response.data().isEmpty()) {
                if (response != null && response.information() != null) {
                    log.warn("Alpha Vantage API message for {}: {}", commodity, response.information());
                }
                if (response != null && response.note() != null) {
                    log.warn("Alpha Vantage rate limit note for {}: {}", commodity, response.note());
                }
                log.warn("Empty response for {}", commodity);
                return List.of();
            }

            List<FinanceResponse.Entry> validEntries = response.data().stream()
                    .filter(e -> e.value() != null && !".".equals(e.value()))
                    .toList();

            if (validEntries.isEmpty()) {
                log.warn("No valid data point for {}", commodity);
                return List.of();
            }

            return validEntries.stream().map(entry -> {
                BigDecimal pricePerTon = new BigDecimal(entry.value());
                BigDecimal priceUsd    = toUsdPerBrazilianUnit(commodity, pricePerTon);
                BigDecimal priceBrl    = priceUsd.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

                return new MarketPrice(
                        commodity, priceBrl, priceUsd, exchangeRate,
                        UNITS.get(commodity), "ALPHA_VANTAGE",
                        LocalDate.parse(entry.date())
                );
            }).toList();

        } catch (MarketDataException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to fetch {}: {}", commodity, e.getMessage());
            return List.of();
        }
    }

    private BigDecimal toUsdPerBrazilianUnit(Commodity commodity, BigDecimal rawPrice) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_UP);
        return switch (commodity) {
            case SOJA, TRIGO ->
                    rawPrice.divide(SACAS_POR_BUSHEL_SOJA_TRIGO, mc).setScale(4, RoundingMode.HALF_UP);
            case MILHO ->
                    rawPrice.divide(SACAS_POR_BUSHEL_MILHO, mc).setScale(4, RoundingMode.HALF_UP);
            case CAFE ->
                rawPrice.divide(BigDecimal.valueOf(100), mc)
                        .multiply(LBS_POR_SACA, mc)
                        .setScale(4, RoundingMode.HALF_UP);
            case ALGODAO ->
                rawPrice.divide(BigDecimal.valueOf(100), mc)
                        .multiply(LBS_POR_ARROBA, mc)
                        .setScale(4, RoundingMode.HALF_UP);
            case ACUCAR ->
                rawPrice.divide(BigDecimal.valueOf(100), mc)
                        .multiply(LBS_POR_KG, mc)
                        .multiply(BigDecimal.valueOf(50), mc)
                        .setScale(4, RoundingMode.HALF_UP);
            case BOI_GORDO ->
                    throw new MarketDataException("no source for BOI_GORDO");
        };
    }

    private FinanceResponse fetchWithResilience(String function) {
        try {
            return Decorators
                    .ofCompletionStage(() -> CompletableFuture.supplyAsync(() -> makeRequest(function)))
                    .withRateLimiter(rateLimiter)
                    .withRetry(retry, scheduler)
                    .withCircuitBreaker(circuitBreaker)
                    .withTimeLimiter(timeLimiter, scheduler)
                    .get()
                    .toCompletableFuture()
                    .join();
        } catch (Exception e) {
            log.error("Failed to fetch function {}: {}", function, e.getMessage());
            throw new MarketDataException("Market data unavailable for function: " + function, e);
        }
    }

    private FinanceResponse makeRequest(String function) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(QUERY_PATH)
                        .queryParam("function", function)
                        .queryParam("interval", "monthly")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .body(FinanceResponse.class);
    }

    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }
}