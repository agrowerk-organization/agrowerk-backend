package tech.agrowerk.application.dto.open_meteo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record Daily(
        List<String> time,

        @JsonProperty("weather_code")
        List<Integer> weatherCode,

        @JsonProperty("temperature_2m_max")
        List<BigDecimal> temperature2mMax,

        @JsonProperty("temperature_2m_min")
        List<BigDecimal> temperature2mMin,

        @JsonProperty("precipitation_sum")
        List<BigDecimal> precipitationSum,

        @JsonProperty("precipitation_probability_max")
        List<Integer> precipitationProbabilityMax,

        @JsonProperty("wind_speed_10m_max")
        List<BigDecimal> windSpeed10mMax,

        @JsonProperty("wind_direction_10m_dominant")
        List<Integer> windDirection10mDominant,

        @JsonProperty("et0_fao_evapotranspiration")
        List<BigDecimal> et0FaoEvapotranspiration,

        @JsonProperty("uv_index_max")
        List<BigDecimal> uvIndexMax
) {}