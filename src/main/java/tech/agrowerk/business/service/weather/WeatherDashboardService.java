package tech.agrowerk.business.service.weather;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.*;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WeatherDashboardService {

    private final WeatherCacheService cacheService;
    private final WeatherLocationRepository locationRepository;

    private static final int DEFAULT_FORECAST_DAYS = 7;

    public WeatherDashboardService(WeatherCacheService cacheService, WeatherLocationRepository locationRepository) {
        this.cacheService = cacheService;
        this.locationRepository = locationRepository;
    }

    public Dashboard getDashboard(UUID locationId) {

        WeatherLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        Current current = cacheService.getCurrentWeather(locationId);

        List<Forecast> forecast =
                cacheService.getForecast(locationId, DEFAULT_FORECAST_DAYS);

        List<Alert> alerts =
                cacheService.getActiveAlerts(locationId);

        Statistics statistics =
                cacheService.calculateStatistics(locationId);

        return Dashboard.builder()
                .locationId(location.getId())
                .locationName(location.getName())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .current(current)
                .dailyForecast(forecast)
                .activeAlerts(alerts)
                .statistics(statistics)
                .lastUpdate(LocalDateTime.now())
                .build();
    }
}
