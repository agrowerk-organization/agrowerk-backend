package tech.agrowerk.application.dto.weather;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Current(
        UUID id,
        UUID locationId,
        String locationName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant timestamp,
        BigDecimal temperature,
        BigDecimal feelsLike,
        Integer humidity,
        Integer pressure,
        BigDecimal windSpeed,
        Integer windDirection,
        BigDecimal windGusts,
        Integer clouds,
        Integer visibility,
        BigDecimal uvIndex,
        BigDecimal rainfall,
        BigDecimal snowfall,
        String weatherCondition,
        String weatherDescription,
        Integer weatherCode,
        String weatherIcon,
        String source,
        Boolean fromCache,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant fetchedAt
) {}