package tech.agrowerk.application.dto.weather;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record Forecast(
        UUID id,
        UUID locationId,
        @JsonFormat(pattern = "yyyy-MM-dd") String forecastDate,
        Integer forecastHour,
        BigDecimal temperatureMin,
        BigDecimal temperatureMax,
        BigDecimal temperatureAvg,
        Integer humidityAvg,
        Integer humidityMin,
        Integer humidityMax,
        Integer rainfallProbability,
        BigDecimal rainfallAmount,
        BigDecimal rainfallAccumulated7d,
        BigDecimal windSpeedAvg,
        BigDecimal windSpeedMax,
        Integer windDirection,
        BigDecimal uvIndexMax,
        String weatherCondition,
        String weatherDescription,
        Integer weatherCode,
        String weatherIcon,
        BigDecimal evapotranspiration,
        BigDecimal soilMoisture,
        BigDecimal soilTemperature,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant fetchedAt
) {}