package tech.agrowerk.application.dto.open_meteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record Current(
        String time,
        Integer interval,

        @JsonProperty("temperature_2m")
        BigDecimal temperature2m,

        @JsonProperty("relative_humidity_2m")
        Integer relativeHumidity2m,

        @JsonProperty("apparent_temperature")
        BigDecimal apparentTemperature,

        BigDecimal precipitation,
        BigDecimal rain,
        BigDecimal snowfall,

        @JsonProperty("weather_code")
        Integer weatherCode,

        @JsonProperty("cloud_cover")
        Integer cloudCover,

        @JsonProperty("pressure_msl")
        BigDecimal pressureMsl,

        @JsonProperty("surface_pressure")
        BigDecimal surfacePressure,

        @JsonProperty("wind_speed_10m")
        BigDecimal windSpeed10m,

        @JsonProperty("wind_direction_10m")
        Integer windDirection10m,

        @JsonProperty("wind_gusts_10m")
        BigDecimal windGusts10m
) {}