package tech.agrowerk.application.dto.open_meteo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record Hourly(
    List<String> time,

    @JsonProperty("temperature_2m")
    List<BigDecimal> temperature2m,

    @JsonProperty("relative_humidity_2m")
    List<Integer> relativeHumidity2m,

    @JsonProperty("precipitation_probability")
    List<Integer> precipitationProbability,

    List<BigDecimal> precipitation,

    @JsonProperty("weather_code")
    List<Integer> weatherCode,

    @JsonProperty("wind_speed_10m")
   List<BigDecimal> windSpeed10m,

    @JsonProperty("wind_direction_10m")
    List<Integer> windDirection10m
) {}