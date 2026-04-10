package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.util.List;

@Component
@Slf4j
public class WeatherScheduler {

    private final WeatherFetchService weatherFetchService;
    private final WeatherLocationRepository weatherLocationRepository;

    private static final int DEFAULT_FORECAST_DAYS = 7;

    public WeatherScheduler(WeatherFetchService weatherFetchService, WeatherLocationRepository weatherLocationRepository) {
        this.weatherFetchService = weatherFetchService;
        this.weatherLocationRepository = weatherLocationRepository;
    }

    @Scheduled(cron = "${weather.scheduler.cron:0 */10 * * * *}", zone = "America/Fortaleza")
    @Transactional
    public void scheduledWeatherUpdate() {
        List<WeatherLocation> activeLocations = weatherLocationRepository.findByActiveTrue();
        log.info("Weather sync started for {} active locations", activeLocations.size());

        for (WeatherLocation location : activeLocations) {
            try {
                weatherFetchService.fetchAndSaveCurrentWeather(location);
                weatherFetchService.fetchAndSaveForecast(location, DEFAULT_FORECAST_DAYS);
            } catch (Exception e) {
                log.error("Failed to update weather for {}", location.getName(), e);
            }
        }
    }
}
