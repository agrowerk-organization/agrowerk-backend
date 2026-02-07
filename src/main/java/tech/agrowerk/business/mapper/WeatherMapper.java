package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.open_meteo.OpenMeteoResponse;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.application.dto.weather.Current;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.infrastructure.model.*;
import tech.agrowerk.infrastructure.model.weather.WeatherAlert;
import tech.agrowerk.infrastructure.model.weather.WeatherCurrent;
import tech.agrowerk.infrastructure.model.weather.WeatherForecast;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


@Component
public class WeatherMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public WeatherCurrent toCurrentEntity(OpenMeteoResponse apiResponse, WeatherLocation location) {
        if (apiResponse == null || apiResponse.current() == null) {
            return null;
        }

        var current = apiResponse.current();

        return WeatherCurrent.builder()
                .location(location)
                .timestamp(Instant.parse(current.time()))
                .temperature(current.temperature2m())
                .feelsLike(current.apparentTemperature())
                .humidity(current.relativeHumidity2m())
                .pressure(current.pressureMsl() != null ? current.pressureMsl().intValue() : null)
                .windSpeed(current.windSpeed10m())
                .windDirection(current.windDirection10m())
                .windGusts(current.windGusts10m())
                .clouds(current.cloudCover())
                .rainfall(current.rain())
                .snowfall(current.snowfall())
                .weatherCode(current.weatherCode())
                .weatherCondition(getWeatherCondition(current.weatherCode()))
                .weatherDescription(getWeatherDescription(current.weatherCode()))
                .source("open-meteo")
                .build();
    }

    public Current toCurrentDTO(WeatherCurrent entity) {
        if (entity == null) return null;

        return Current.builder()
                .id(entity.getId())
                .locationId(entity.getLocation().getId())
                .locationName(entity.getLocation().getName())
                .timestamp(entity.getTimestamp())
                .temperature(entity.getTemperature())
                .feelsLike(entity.getFeelsLike())
                .humidity(entity.getHumidity())
                .pressure(entity.getPressure())
                .windSpeed(entity.getWindSpeed())
                .windDirection(entity.getWindDirection())
                .windGusts(entity.getWindGusts())
                .clouds(entity.getClouds())
                .visibility(entity.getVisibility())
                .uvIndex(entity.getUvIndex())
                .rainfall(entity.getRainfall())
                .snowfall(entity.getSnowfall())
                .weatherCondition(entity.getWeatherCondition())
                .weatherDescription(entity.getWeatherDescription())
                .weatherCode(entity.getWeatherCode())
                .weatherIcon(getWeatherIcon(entity.getWeatherCode()))
                .source(entity.getSource())
                .fromCache(true)
                .fetchedAt(entity.getCreatedAt())
                .build();
    }

    public List<WeatherForecast> toForecastEntities(OpenMeteoResponse apiResponse, WeatherLocation location) {
        if (apiResponse == null || apiResponse.daily() == null) return new ArrayList<>();

        List<WeatherForecast> forecasts = new ArrayList<>();
        var daily = apiResponse.daily();

        for (int i = 0; i < daily.time().size(); i++) {
            BigDecimal min = daily.temperature2mMin().get(i);
            BigDecimal max = daily.temperature2mMax().get(i);
            BigDecimal avg = (min != null && max != null)
                    ? min.add(max).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                    : null;

            forecasts.add(WeatherForecast.builder()
                    .location(location)
                    .forecastDate(LocalDate.parse(daily.time().get(i)))
                    .temperatureMin(min)
                    .temperatureMax(max)
                    .temperatureAvg(avg)
                    .rainfallAmount(daily.precipitationSum().get(i))
                    .rainfallProbability(daily.precipitationProbabilityMax().get(i))
                    .windSpeedMax(daily.windSpeed10mMax().get(i))
                    .windDirection(daily.windDirection10mDominant().get(i))
                    .uvIndexMax(daily.uvIndexMax().get(i))
                    .evapotranspiration(daily.et0FaoEvapotranspiration().get(i))
                    .weatherCode(daily.weatherCode().get(i))
                    .weatherCondition(getWeatherCondition(daily.weatherCode().get(i)))
                    .weatherDescription(getWeatherDescription(daily.weatherCode().get(i)))
                    .createdAt(Instant.now())
                    .build());
        }
        return forecasts;
    }

    public Forecast toForecastDTO(WeatherForecast entity) {
        if (entity == null) return null;

        return Forecast.builder()
                .id(entity.getId())
                .locationId(entity.getLocation().getId())
                .forecastDate(entity.getForecastDate().toString())
                .temperatureMin(entity.getTemperatureMin())
                .temperatureMax(entity.getTemperatureMax())
                .temperatureAvg(entity.getTemperatureAvg())
                .rainfallProbability(entity.getRainfallProbability())
                .rainfallAmount(entity.getRainfallAmount())
                .evapotranspiration(entity.getEvapotranspiration())
                .weatherCode(entity.getWeatherCode())
                .weatherIcon(getWeatherIcon(entity.getWeatherCode()))
                .fetchedAt(entity.getCreatedAt())
                .build();
    }

    public Alert toAlertDTO(WeatherAlert entity) {
        if (entity == null) {
            return null;
        }

        return Alert.builder()
                .id(entity.getId())
                .locationId(entity.getLocation().getId())
                .locationName(entity.getLocation().getName())
                .alertType(entity.getAlertType().name())
                .alertTypeDisplayName(entity.getAlertType().getDisplayName())
                .severity(entity.getSeverity().name())
                .severityDisplayName(entity.getSeverity().getDisplayName())
                .severityColor(entity.getSeverity().getColorHex())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .recommendedActions(entity.getRecommendedActions())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .isActive(entity.getIsActive())
                .notified(entity.getNotified())
                .source(entity.getSource())
                .createdAt(entity.getCreatedAt())
                .build();
    }


    private String getWeatherCondition(Integer code) {
        if (code == null) return "Unknown";

        return switch (code) {
            case 0 -> "Clear";
            case 1, 2, 3 -> "Partly Cloudy";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing Rain";
            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow Grains";
            case 80, 81, 82 -> "Rain Showers";
            case 85, 86 -> "Snow Showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with Hail";
            default -> "Unknown";
        };
    }

    private String getWeatherDescription(Integer code) {
        if (code == null) return "Condição desconhecida";

        return switch (code) {
            case 0 -> "Céu limpo";
            case 1 -> "Predominantemente limpo";
            case 2 -> "Parcialmente nublado";
            case 3 -> "Nublado";
            case 45, 48 -> "Neblina";
            case 51 -> "Garoa leve";
            case 53 -> "Garoa moderada";
            case 55 -> "Garoa intensa";
            case 61 -> "Chuva leve";
            case 63 -> "Chuva moderada";
            case 65 -> "Chuva forte";
            case 66, 67 -> "Chuva congelante";
            case 71 -> "Neve leve";
            case 73 -> "Neve moderada";
            case 75 -> "Neve intensa";
            case 80 -> "Pancadas de chuva leves";
            case 81 -> "Pancadas de chuva moderadas";
            case 82 -> "Pancadas de chuva fortes";
            case 95 -> "Tempestade";
            case 96, 99 -> "Tempestade com granizo";
            default -> "Condição desconhecida";
        };
    }

    private String getWeatherIcon(Integer code) {
        if (code == null) return "wi-na";

        return switch (code) {
            case 0 -> "wi-day-sunny";
            case 1, 2 -> "wi-day-cloudy";
            case 3 -> "wi-cloudy";
            case 45, 48 -> "wi-fog";
            case 51, 53, 55 -> "wi-sprinkle";
            case 61, 63, 65 -> "wi-rain";
            case 66, 67 -> "wi-rain-mix";
            case 71, 73, 75, 77 -> "wi-snow";
            case 80, 81, 82 -> "wi-showers";
            case 85, 86 -> "wi-snow-wind";
            case 95 -> "wi-thunderstorm";
            case 96, 99 -> "wi-storm-showers";
            default -> "wi-na";
        };
    }
}