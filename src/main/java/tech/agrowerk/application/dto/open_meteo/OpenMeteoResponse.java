package tech.agrowerk.application.dto.open_meteo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record OpenMeteoResponse(
    BigDecimal latitude,
    BigDecimal longitude,

    @JsonProperty("generationtime_ms")
    Double generationtimeMs,

    @JsonProperty("utc_offset_seconds")
    Integer utcOffsetSeconds,

    String timezone,
    @JsonProperty("timezone_abbreviation")

    String timezoneAbbreviation,
    BigDecimal elevation,

    @JsonProperty("current_units")
    CurrentUnits currentUnits,

    Current current,

    @JsonProperty("hourly_units")
    HourlyUnits hourlyUnits,

    Hourly hourly,

    @JsonProperty("daily_units")
    DailyUnits dailyUnits,

    Daily daily
) {
}
