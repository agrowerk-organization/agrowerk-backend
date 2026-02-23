package tech.agrowerk.business.service.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.application.dto.weather.Current;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.application.dto.weather.Statistics;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WeatherCacheService {

    private final WeatherService weatherService;

    public WeatherCacheService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Cacheable(value = "weatherCurrent", key = "#locationId", unless = "#result == null")
    public Current getCurrentWeather(UUID locationId) {
        return weatherService.getCurrentWeatherInternal(locationId);
    }

    @Cacheable(value = "weatherForecast",
            key = "#locationId + '-' + #days",
            unless = "#result == null")
    public List<Forecast> getForecast(UUID locationId, int days) {
        return weatherService.getForecastInternal(locationId, days);
    }

    @Cacheable(value = "weatherAlerts", key = "#locationId", unless = "#result == null")
    public List<Alert> getActiveAlerts(UUID locationId) {
        return weatherService.getActiveAlertsInternal(locationId);
    }

    @Cacheable(value = "weatherStatistics", key = "#locationId", unless = "#result == null")
    public Statistics calculateStatistics(UUID locationId) {
        return weatherService.calculateStatisticsInternal(locationId);
    }
}
