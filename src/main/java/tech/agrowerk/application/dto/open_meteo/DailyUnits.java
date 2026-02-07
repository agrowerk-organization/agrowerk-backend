package tech.agrowerk.application.dto.open_meteo;

public record DailyUnits(
        String time,
        String weather_code,
        String temperature_2m_max,
        String temperature_2m_min,
        String precipitation_sum,
        String precipitation_probability_max,
        String wind_speed_10m_max,
        String wind_direction_10m_dominant,
        String et0_fao_evapotranspiration,
        String uv_index_max
) {
}
