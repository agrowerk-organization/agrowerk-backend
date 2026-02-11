package tech.agrowerk.business.service.weather;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.open_meteo.OpenMeteoResponse;
import tech.agrowerk.application.dto.weather.*;
import tech.agrowerk.business.mapper.WeatherMapper;
import tech.agrowerk.infrastructure.exception.local.WeatherApiException;
import tech.agrowerk.infrastructure.model.weather.WeatherCurrent;
import tech.agrowerk.infrastructure.model.weather.WeatherForecast;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertSeverity;
import tech.agrowerk.infrastructure.repository.weather.WeatherAlertRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherCurrentRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherForecastRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final OpenMeteoClient openMeteoClient;
    private final WeatherLocationRepository locationRepository;
    private final WeatherCurrentRepository currentRepository;
    private final WeatherForecastRepository forecastRepository;
    private final WeatherAlertRepository alertRepository;
    private final WeatherMapper weatherMapper;
    private final WeatherAlertService alertService;

    private static final int DEFAULT_FORECAST_DAYS = 7;
    private static final int STATISTICS_PERIOD_DAYS = 7;
    private static final int STATISTICS_PERIOD_MONTHS = 30;


    @CircuitBreaker(name = "weatherApiCircuitBreaker", fallbackMethod = "getCurrentWeatherFallback")
    @Transactional(readOnly = true, noRollbackFor = {WeatherApiException.class, CallNotPermittedException.class})
    public Current getCurrentWeatherInternal(UUID locationId) {
        WeatherLocation location = findLocationOrThrow(locationId);

        Optional<WeatherCurrent> cachedData = currentRepository.findTopByLocationOrderByTimestampDesc(location);

        if (cachedData.isPresent() &&
                cachedData.get().getTimestamp().isAfter(Instant.now().minus(10, ChronoUnit.MINUTES))) {
            return weatherMapper.toCurrentDTO(cachedData.get(), false);
        }

        return fetchAndSaveCurrentWeather(location);
    }

    public Current getCurrentWeatherFallback(UUID locationId, Throwable t) {

        WeatherLocation location = findLocationOrThrow(locationId);

        return currentRepository.findTopByLocationOrderByTimestampDesc(location)
                .map(weather -> weatherMapper.toCurrentDTO(weather, true))
                .orElseThrow(() -> new WeatherApiException("API unavailable and no historical data found in the database."));
    }

    @Transactional(readOnly = true)
    public List<Forecast> getForecastInternal(UUID locationId, int days) {

        validateForecastDays(days);

        WeatherLocation location = findLocationOrThrow(locationId);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        List<WeatherForecast> cachedForecasts =
                forecastRepository.findByLocationAndForecastDateBetweenAndForecastHourIsNull(
                        location, startDate, endDate
                );

        if (!cachedForecasts.isEmpty() && cachedForecasts.size() >= days) {
            return cachedForecasts.stream()
                    .map(weatherMapper::toForecastDTO)
                    .limit(days)
                    .toList();
        }

        return fetchAndSaveForecast(location, days);
    }


    @Transactional(readOnly = true)
    public List<Alert> getActiveAlertsInternal(UUID locationId) {

        WeatherLocation location = findLocationOrThrow(locationId);

        return alertRepository.findByLocationAndIsActiveTrue(location)
                .stream()
                .map(weatherMapper::toAlertDTO)
                .toList();
    }


    @Transactional(readOnly = true)
    public Statistics calculateStatisticsInternal(UUID locationId) {

        WeatherLocation location = findLocationOrThrow(locationId);

        Instant sevenDaysAgo = Instant.now().minus(STATISTICS_PERIOD_DAYS, ChronoUnit.DAYS);
        Instant thirtyDaysAgo = Instant.now().minus(STATISTICS_PERIOD_MONTHS, ChronoUnit.DAYS);

        List<WeatherCurrent> last7Days =
                currentRepository.findByLocationAndTimestampAfterOrderByTimestampDesc(
                        location, sevenDaysAgo);

        List<WeatherCurrent> last30Days =
                currentRepository.findByLocationAndTimestampAfterOrderByTimestampDesc(
                        location, thirtyDaysAgo);

        if (last7Days.isEmpty()) return buildEmptyStatistics();

        BigDecimal avgTemp7d = calculateAverageTemperature(last7Days);
        BigDecimal totalRain7d = calculateTotalRainfall(last7Days);
        BigDecimal totalRain30d = calculateTotalRainfall(last30Days);

        long totalAlerts = alertRepository.countByLocation(location);
        long criticalAlerts =
                alertRepository.countByLocationAndSeverity(location, WeatherAlertSeverity.CRITICAL);

        BigDecimal waterStressIndex = calculateWaterStressIndex(totalRain7d, avgTemp7d);

        return Statistics.builder()
                .avgTemperatureLast7Days(avgTemp7d)
                .totalRainfallLast7Days(totalRain7d)
                .totalRainfallLast30Days(totalRain30d)
                .totalAlerts((int) totalAlerts)
                .criticalAlerts((int) criticalAlerts)
                .waterStressIndex(waterStressIndex)
                .waterStressLevel(getWaterStressLevel(waterStressIndex))
                .build();
    }


    @Transactional
    public Current fetchAndSaveCurrentWeather(WeatherLocation location) {

        OpenMeteoResponse apiResponse =
                openMeteoClient.fetchWeatherData(location.getLatitude(), location.getLongitude());

        WeatherCurrent entity = weatherMapper.toCurrentEntity(apiResponse, location);
        entity = currentRepository.save(entity);

        alertService.processWeatherDataForAlerts(entity);

        return weatherMapper.toCurrentDTO(entity, false);
    }

    @Transactional
    public List<Forecast> fetchAndSaveForecast(WeatherLocation location, int days) {

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);

        forecastRepository.deleteByLocationAndForecastDateBetween(location, start, end);

        OpenMeteoResponse apiResponse =
                openMeteoClient.fetchWeatherData(location.getLatitude(), location.getLongitude());

        List<WeatherForecast> forecasts =
                forecastRepository.saveAll(weatherMapper.toForecastEntities(apiResponse, location));

        return forecasts.stream()
                .filter(f -> f.getForecastHour() == null)
                .limit(days)
                .map(weatherMapper::toForecastDTO)
                .toList();
    }


    @Scheduled(cron = "${weather.scheduler.cron:0 */10 * * * *}")
    @Transactional
    public void scheduledWeatherUpdate() {

        List<WeatherLocation> activeLocations = locationRepository.findByActiveTrue();

        for (WeatherLocation location : activeLocations) {

            try {
                fetchAndSaveCurrentWeather(location);
                fetchAndSaveForecast(location, DEFAULT_FORECAST_DAYS);
            } catch (Exception e) {
                log.error("Failed to update weather {}", location.getName(), e);
            }
        }
    }


    private WeatherLocation findLocationOrThrow(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + id));
    }

    private void validateForecastDays(int days) {
        if (days < 1 || days > 7) throw new IllegalArgumentException();
    }

    private BigDecimal calculateAverageTemperature(List<WeatherCurrent> data) {
        if (data.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = data.stream()
                .map(WeatherCurrent::getTemperature)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(data.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalRainfall(List<WeatherCurrent> data) {
        return data.stream()
                .map(WeatherCurrent::getRainfall)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateWaterStressIndex(BigDecimal rainfall7d, BigDecimal avgTemp) {
        return rainfall7d.add(avgTemp).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private String getWaterStressLevel(BigDecimal index) {
        if (index.compareTo(BigDecimal.valueOf(0.25)) < 0) return "LOW";
        if (index.compareTo(BigDecimal.valueOf(0.5)) < 0) return "MEDIUM";
        if (index.compareTo(BigDecimal.valueOf(0.75)) < 0) return "HIGH";
        return "CRITICAL";
    }

    private Statistics buildEmptyStatistics() {
        return Statistics.builder().waterStressLevel("UNKNOWN").build();
    }
}
