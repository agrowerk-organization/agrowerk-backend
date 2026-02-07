package tech.agrowerk.infrastructure.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Duration;

@Slf4j
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(100)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(60))
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(
                        ConnectException.class,
                        SocketTimeoutException.class,
                        IOException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class
                )
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        registry.circuitBreaker("weatherApiCircuitBreaker")
                .getEventPublisher()
                .onStateTransition(event ->
                        log.warn("Weather API Circuit Breaker state changed: {} -> {}",
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState())
                )
                .onFailureRateExceeded(event ->
                        log.error("Weather API Circuit Breaker failure rate exceeded: {}%",
                                event.getFailureRate())
                );
        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .intervalFunction(IntervalFunction
                        .ofExponentialBackoff(Duration.ofSeconds(1), 2))
                .retryExceptions(
                        java.net.ConnectException.class,
                        java.net.SocketTimeoutException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class
                )
                .build();

        RetryRegistry registry = RetryRegistry.of(config);

        registry.retry("weatherApiRetry")
                .getEventPublisher()
                .onRetry(event ->
                        log.warn("Weather API retry attempt {} of {}",
                                event.getNumberOfRetryAttempts(),
                                3)
                );

        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10))
                .cancelRunningFuture(true)
                .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);

        registry.timeLimiter("weatherApiTimeLimiter")
                .getEventPublisher()
                .onTimeout(event ->
                        log.error("Weather API call timeout after 10 seconds")
                );

        return registry;
    }
}
