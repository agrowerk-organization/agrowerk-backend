package tech.agrowerk.application.dto.weather;

import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertSeverity;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertType;

import java.time.Instant;
import java.util.Map;

public record AlertStatistics(
        long totalActive,
        long totalResolved,
        Map<WeatherAlertSeverity, Long> bySeverity,
        Map<WeatherAlertType, Long> byType,
        Instant lastUpdated
) {
}
