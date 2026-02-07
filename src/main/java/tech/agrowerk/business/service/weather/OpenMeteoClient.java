package tech.agrowerk.business.service.weather;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tech.agrowerk.application.dto.open_meteo.OpenMeteoResponse;
import tech.agrowerk.infrastructure.exception.local.WeatherApiException;

import java.math.BigDecimal;

@Service
@Slf4j
public class OpenMeteoClient {

    @Value("${openmeteo.api.base-url}")
    private String baseUrl;

    private static final String CURRENT_PARAMS = String.join(",",
            "temperature_2m",
            "relative_humidity_2m",
            "apparent_temperature",
            "precipitation",
            "rain",
            "snowfall",
            "weather_code",
            "cloud_cover",
            "pressure_msl",
            "surface_pressure",
            "wind_speed_10m",
            "wind_direction_10m",
            "wind_gusts_10m"
    );

    private static final String HOURLY_PARAMS = String.join(",",
            "temperature_2m",
            "relative_humidity_2m",
            "precipitation_probability",
            "precipitation",
            "weather_code",
            "wind_speed_10m",
            "wind_direction_10m"
    );

    private static final String DAILY_PARAMS = String.join(",",
            "weather_code",
            "temperature_2m_max",
            "temperature_2m_min",
            "precipitation_sum",
            "precipitation_probability_max",
            "wind_speed_10m_max",
            "wind_direction_10m_dominant",
            "et0_fao_evapotranspiration",
            "uv_index_max"
    );

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public OpenMeteoClient(
            RestClient.Builder builder,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    String errorMsg = String.format(
                            "Client error calling Open-Meteo: %s - %s",
                            response.getStatusCode(),
                            response.getStatusText()
                    );
                    log.error(errorMsg);
                    throw new WeatherApiException(errorMsg);
                })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    String errorMsg = String.format(
                            "Server error from Open-Meteo: %s - %s",
                            response.getStatusCode(),
                            response.getStatusText()
                    );
                    log.error(errorMsg);
                    throw new WeatherApiException(errorMsg);
                })
                .build();

        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("weatherApi");
        this.retry = retryRegistry.retry("weatherApi");

        setupEventListeners();
    }

    public OpenMeteoResponse fetchWeatherData(BigDecimal latitude, BigDecimal longitude) {
        long startTime = System.currentTimeMillis();

        log.debug("Fetching weather data for lat={}, lon={}", latitude, longitude);

        try {
            OpenMeteoResponse response = Decorators
                    .ofSupplier(() -> makeApiCall(latitude, longitude))
                    .withCircuitBreaker(circuitBreaker)
                    .withRetry(retry)
                    .get();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Weather data fetched successfully in {}ms (lat={}, lon={})",
                    duration, latitude, longitude);

            return response;

        } catch (Exception e) {
            log.error("Failed to fetch weather data after all retries: {}", e.getMessage());
            throw new WeatherApiException("Weather API unavailable", e);
        }
    }

    private OpenMeteoResponse makeApiCall(BigDecimal latitude, BigDecimal longitude) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current", CURRENT_PARAMS)
                        .queryParam("hourly", HOURLY_PARAMS)
                        .queryParam("daily", DAILY_PARAMS)
                        .queryParam("forecast_days", 7)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);
    }

    private void setupEventListeners() {
        // Circuit Breaker events
        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                        log.warn("⚡ Circuit Breaker state transition: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState())
                )
                .onFailureRateExceeded(event ->
                        log.error("🔴 Circuit Breaker failure rate exceeded: {}%",
                                event.getFailureRate())
                )
                .onSlowCallRateExceeded(event ->
                        log.warn("🐌 Circuit Breaker slow call rate exceeded: {}%",
                                event.getSlowCallRate())
                );

        // Retry events
        retry.getEventPublisher()
                .onRetry(event ->
                        log.warn("🔄 Retry attempt {} of {} for Open-Meteo API",
                                event.getNumberOfRetryAttempts(),
                                retry.getRetryConfig().getMaxAttempts())
                )
                .onError(event ->
                        log.error("❌ All retry attempts exhausted: {}",
                                event.getLastThrowable().getMessage())
                );
    }


    public String getCircuitBreakerState() {
        return circuitBreaker.getState().name();
    }

    public CircuitBreakerMetrics getMetrics() {
        var metrics = circuitBreaker.getMetrics();
        return new CircuitBreakerMetrics(
                circuitBreaker.getState().name(),
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfSuccessfulCalls(),
                metrics.getNumberOfFailedCalls(),
                metrics.getNumberOfSlowCalls()
        );
    }

    public void resetCircuitBreaker() {
        circuitBreaker.reset();
        log.info("Circuit Breaker reset to CLOSED state");
    }

    public void forceOpen() {
        circuitBreaker.transitionToForcedOpenState();
        log.warn("Circuit Breaker forced to OPEN state");
    }

    public record CircuitBreakerMetrics(
            String state,
            float failureRate,
            float slowCallRate,
            int successfulCalls,
            int failedCalls,
            int slowCalls
    ) {}
}