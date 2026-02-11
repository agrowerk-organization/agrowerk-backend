package tech.agrowerk.application.dto.weather;

public record CircuitBreakerMetrics(
        String state,
        float failureRate,
        float slowCallRate,
        int successfulCalls,
        int failedCalls,
        int slowCalls
) {}