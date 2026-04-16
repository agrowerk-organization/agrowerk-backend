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

    private static final String BASE_URL  = "https://api.stlouisfed.org";
    private static final String QUERY_PATH = "/fred/series/observations";

    private static final BigDecimal SACAS_POR_TON   = BigDecimal.valueOf(16.6667);
    private static final BigDecimal ARROBAS_POR_TON = BigDecimal.valueOf(66.6667);
    private static final BigDecimal LBS_POR_KG = BigDecimal.valueOf(2.20462);
    private static final BigDecimal KG_POR_ARROBA = BigDecimal.valueOf(15);

    private static final Map<Commodity, String> UNITS = Map.of(
            Commodity.SOJA,    "R$/saca",
            Commodity.MILHO,   "R$/saca",
            Commodity.CAFE,    "R$/saca",
            Commodity.TRIGO,   "R$/saca",
            Commodity.ALGODAO, "R$/arroba",
            Commodity.ACUCAR, "R$/saca",
            Commodity.BOI_GORDO, "R$/arroba"
    );

    @Value("${fred.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final ExchangeRateService exchangeRateService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
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
                    throw new MarketDataException("Financial API client error: " + res.getStatusCode());
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new MarketDataException("Financial API server error: " + res.getStatusCode());
                })
                .build();

        this.exchangeRateService = exchangeRateService;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retry          = retryRegistry.retry("marketDataRetry");
        this.timeLimiter    = timeLimiterRegistry.timeLimiter("marketDataTimeLimiter");
        this.rateLimiter = rateLimiterRegistry.rateLimiter("marketDataRateLimiter");
    }

    public List<MarketPrice> fetchAll() {
        BigDecimal exchangeRate = exchangeRateService.getUsdToBrlForDate(LocalDate.now());
        log.info("Exchange rate USD/BRL: {}", exchangeRate);

        return Arrays.stream(Commodity.values())
                .filter(Commodity::hasFredSource)
                .map(this::fetchCommodity)
                .flatMap(List::stream)
                .toList();
    }

    private List<MarketPrice> fetchCommodity(Commodity commodity) {
        log.debug("Fetching {} ({})", commodity, commodity.getFredSeriesId());

        try {
            FinanceResponse response = fetchWithResilience(commodity.getFredSeriesId(), commodity);

            if (response == null || response.observations() == null || response.observations().isEmpty()) {
                log.warn("Empty response for {}", commodity);
                return List.of();
            }

            List<FinanceResponse.Entry> validEntries = response.observations().stream()
                    .filter(e -> e.value() != null && !".".equals(e.value()))
                    .toList();

            if (validEntries.isEmpty()) {
                log.warn("No valid data point for {}", commodity);
                return List.of();
            }

            LocalDate start = LocalDate.parse(validEntries.getFirst().date());
            LocalDate end = LocalDate.now();

            Map<LocalDate, BigDecimal> ratesMap = exchangeRateService.getRatesForPeriod(start, end);

            return validEntries.stream().map(entry -> {
                LocalDate referenceDate = LocalDate.parse(entry.date());

                BigDecimal historicalRate = ratesMap.getOrDefault(referenceDate,
                        exchangeRateService.getUsdToBrlForDate(referenceDate));

                BigDecimal rawPrice = new BigDecimal(entry.value());
                BigDecimal priceUsd = toUsdPerBrazilianUnit(commodity, rawPrice);
                BigDecimal priceBrl = priceUsd.multiply(historicalRate).setScale(2, RoundingMode.HALF_UP);

                return new MarketPrice(
                        commodity, priceBrl, priceUsd, historicalRate,
                        UNITS.get(commodity), "FRED", referenceDate
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
        MathContext mc = new MathContext(12, RoundingMode.HALF_UP);
        return switch (commodity) {
            case SOJA, MILHO, TRIGO ->
                    rawPrice.divide(SACAS_POR_TON, mc).setScale(4, RoundingMode.HALF_UP);
            case CAFE ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(LBS_POR_KG, mc)
                            .multiply(BigDecimal.valueOf(60), mc)
                            .setScale(4, RoundingMode.HALF_UP);
            case ALGODAO ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(LBS_POR_KG, mc)
                            .multiply(KG_POR_ARROBA, mc)
                            .setScale(4, RoundingMode.HALF_UP);
            case ACUCAR ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(LBS_POR_KG, mc)
                            .multiply(BigDecimal.valueOf(50), mc)
                            .setScale(4, RoundingMode.HALF_UP);
            case BOI_GORDO ->
                    rawPrice.divide(BigDecimal.valueOf(100), mc)
                            .multiply(LBS_POR_KG, mc)
                            .multiply(KG_POR_ARROBA, mc)
                            .setScale(4, RoundingMode.HALF_UP);
            case GENERAL ->
                    throw new MarketDataException("no source for GENERAL");
        };
    }

    private FinanceResponse fetchWithResilience(String seriesId, Commodity commodity) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("marketData_" + commodity.name());

        try {
            return Decorators
                    .ofCompletionStage(() -> CompletableFuture.supplyAsync(() -> makeRequest(seriesId)))
                    .withRateLimiter(rateLimiter)
                    .withRetry(retry, scheduler)
                    .withCircuitBreaker(cb)
                    .withTimeLimiter(timeLimiter, scheduler)
                    .get()
                    .toCompletableFuture()
                    .join();
        } catch (Exception e) {
            log.error("Failed to fetch series {}: {}", seriesId, e.getMessage());
            throw new MarketDataException("Market data unavailable for series: " + seriesId, e);
        }
    }

    private FinanceResponse makeRequest(String seriesId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(QUERY_PATH)
                        .queryParam("series_id", seriesId)
                        .queryParam("api_key", apiKey)
                        .queryParam("file_type", "json")
                        .queryParam("sort_order", "asc")
                        .queryParam("observation_start", "2021-01-04")
                        .build())
                .retrieve()
                .body(FinanceResponse.class);

    }

}