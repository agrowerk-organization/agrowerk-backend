package tech.agrowerk.application.dto.open_meteo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public record HourlyUnits(
    String time,
    String temperature_2m,
    String relative_humidity_2m,
    String precipitation_probability,
    String precipitation,
    String weather_code,
    String wind_speed_10m,
    String wind_direction_10m
) {
}
