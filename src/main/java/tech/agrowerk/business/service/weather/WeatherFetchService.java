package tech.agrowerk.business.service.weather;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.open_meteo.OpenMeteoResponse;
import tech.agrowerk.application.dto.weather.Current;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.business.mapper.weather.WeatherMapper;
import tech.agrowerk.infrastructure.model.weather.WeatherCurrent;
import tech.agrowerk.infrastructure.model.weather.WeatherForecast;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherCurrentRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherForecastRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class WeatherFetchService {

    private final OpenMeteoClient openMeteoClient;
    private final WeatherCurrentRepository weatherCurrentRepository;
    private final WeatherForecastRepository weatherForecastRepository;
    private final WeatherMapper weatherMapper;
    private final WeatherAlertService weatherAlertService;

    public WeatherFetchService(OpenMeteoClient openMeteoClient,
                               WeatherCurrentRepository weatherCurrentRepository,
                               WeatherForecastRepository weatherForecastRepository,
                               WeatherMapper weatherMapper,
                               WeatherAlertService weatherAlertService) {
        this.openMeteoClient = openMeteoClient;
        this.weatherCurrentRepository = weatherCurrentRepository;
        this.weatherForecastRepository = weatherForecastRepository;
        this.weatherMapper = weatherMapper;
        this.weatherAlertService = weatherAlertService;
    }

    @CircuitBreaker(name = "weatherApiCircuitBreaker")
    @Transactional
    public Current fetchAndSaveCurrentWeather(WeatherLocation location) {
        log.info("Fetching current weather for location: {}", location.getId());

        OpenMeteoResponse apiResponse = openMeteoClient.fetchWeatherData(location.getLatitude(), location.getLongitude());

        WeatherCurrent current = weatherMapper.toCurrentEntity(apiResponse, location);
        current = weatherCurrentRepository.save(current);

        weatherAlertService.processWeatherDataForAlerts(current);

        return weatherMapper.toCurrentDTO(current, false);
    }

    @Transactional
    public List<Forecast> fetchAndSaveForecast(WeatherLocation location, int days) {
        log.info("Fetching forecast for location: {} - {} days", location.getId(), days);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);

        weatherForecastRepository.deleteByLocationAndForecastDateBetween(location, start, end);

        OpenMeteoResponse apiResponse = openMeteoClient.fetchWeatherData(location.getLatitude(), location.getLongitude());

        List<WeatherForecast> forecasts = weatherForecastRepository.saveAll(weatherMapper.toForecastEntities(apiResponse, location));

        return forecasts.stream()
                .filter(f -> f.getForecastHour() == null)
                .limit(days)
                .map(weatherMapper::toForecastDTO)
                .toList();
    }
}
