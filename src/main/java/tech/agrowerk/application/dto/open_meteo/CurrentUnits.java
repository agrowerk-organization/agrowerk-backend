package tech.agrowerk.application.dto.open_meteo;


public record CurrentUnits(
    String time,
    String interval,
    String temperature_2m,
    String relative_humidity_2m,
    String apparent_temperature,
    String precipitation,
    String rain,
    String snowfall,
    String weather_code,
    String cloud_cover,
    String pressure_msl,
    String surface_pressure,
    String wind_speed_10m,
    String wind_direction_10m,
    String wind_gusts_10m
) {
}