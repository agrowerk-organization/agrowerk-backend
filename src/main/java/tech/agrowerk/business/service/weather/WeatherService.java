package tech.agrowerk.business.service.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private WeatherService self;
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
    private static final BigDecimal RAINFALL_THRESHOLD = BigDecimal.valueOf(100);
    private static final BigDecimal TEMP_BASELINE = BigDecimal.valueOf(25);
    private static final BigDecimal TEMP_RANGE = BigDecimal.valueOf(15);
    private static final BigDecimal STRESS_THRESHOLD_LOW = BigDecimal.valueOf(0.25);
    private static final BigDecimal STRESS_THRESHOLD_MEDIUM = BigDecimal.valueOf(0.5);
    private static final BigDecimal STRESS_THRESHOLD_HIGH = BigDecimal.valueOf(0.75);

    @Cacheable(value = "weatherCurrent", key = "#locationId", unless = "#result == null", cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    public Current getCurrentWeather(UUID locationId) {
        log.debug("Getting current weather for location: {}", locationId);

        WeatherLocation location = findLocationOrThrow(locationId);

        try {
            return fetchAndSaveCurrentWeather(location);

        } catch (WeatherApiException e) {
            log.warn("Failed to fetch from API, falling back to database for location: {}",
                    locationId, e);

            return getFallbackCurrentWeather(location);
        }
    }

    @Cacheable(value = "weatherForecast", key = "#locationId + '-' + #days", unless = "#result == null", cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    public List<Forecast> getForecast(UUID locationId, int days) {
        validateForecastDays(days);

        log.debug("Getting forecast for location: {}, days: {}", locationId, days);

        WeatherLocation location = findLocationOrThrow(locationId);

        try {
            return fetchAndSaveForecast(location, days);

        } catch (WeatherApiException e) {
            log.warn("Failed to fetch forecast from API, falling back to database", e);
            return getFallbackForecast(location, days);
        }
    }


    @Cacheable(value = "weatherAlerts", key = "#locationId", unless = "#result == null", cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    public List<Alert> getActiveAlerts(UUID locationId) {
        WeatherLocation location = findLocationOrThrow(locationId);

        return alertRepository.findByLocationAndIsActiveTrue(location)
                .stream()
                .map(weatherMapper::toAlertDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Dashboard getDashboard(UUID locationId) {
        log.info("Building weather dashboard for location: {}", locationId);

        WeatherLocation location = findLocationOrThrow(locationId);

        Current current = self.getCurrentWeather(locationId);
        List<Forecast> dailyForecast = self.getForecast(locationId, DEFAULT_FORECAST_DAYS);
        List<Alert> alerts = self.getActiveAlerts(locationId);
        Statistics statistics = self.calculateStatistics(locationId);

        return Dashboard.builder()
                .locationId(location.getId())
                .locationName(location.getName())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .current(current)
                .dailyForecast(dailyForecast)
                .activeAlerts(alerts)
                .statistics(statistics)
                .lastUpdate(LocalDateTime.now())
                .build();
    }

    @Cacheable(value = "weatherStatistics", key = "#locationId", cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    public Statistics calculateStatistics(UUID locationId) {
        WeatherLocation location = findLocationOrThrow(locationId);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(STATISTICS_PERIOD_DAYS);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(STATISTICS_PERIOD_MONTHS);

        List<WeatherCurrent> last7Days = currentRepository
                .findByLocationAndTimestampAfterOrderByTimestampDesc(location, sevenDaysAgo);

        List<WeatherCurrent> last30Days = currentRepository
                .findByLocationAndTimestampAfterOrderByTimestampDesc(location, thirtyDaysAgo);

        if (last7Days.isEmpty()) {
            log.warn("No weather data found for location {} in the last 7 days", locationId);
            return buildEmptyStatistics();
        }

        BigDecimal avgTemp7d = calculateAverageTemperature(last7Days);
        BigDecimal totalRain7d = calculateTotalRainfall(last7Days);
        BigDecimal totalRain30d = calculateTotalRainfall(last30Days);

        long totalAlerts = alertRepository.countByLocation(location);
        long criticalAlerts = alertRepository.countByLocationAndSeverity(
                location,
                WeatherAlertSeverity.CRITICAL
        );

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


    @Scheduled(cron = "${weather.scheduler.cron:0 */10 * * * *}")
    @Caching(evict = {
            @CacheEvict(value = "weatherCurrent", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "weatherCurrent", allEntries = true, cacheManager = "redisCacheManager")
    })
    @Transactional
    public void scheduledWeatherUpdate() {
        log.info("Starting scheduled weather update");

        List<WeatherLocation> activeLocations = locationRepository.findByActiveTrue();

        if (activeLocations.isEmpty()) {
            log.info("No active locations found for weather update");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (WeatherLocation location : activeLocations) {
            try {
                fetchAndSaveCurrentWeather(location);
                fetchAndSaveForecast(location, DEFAULT_FORECAST_DAYS);
                successCount++;

                log.debug("✅ Updated weather data for location: {}", location.getName());

            } catch (Exception e) {
                failureCount++;
                log.error("❌ Failed to update weather for location {}: {}",
                        location.getName(), e.getMessage());
            }
        }

        log.info("Scheduled weather update completed: {} successful, {} failed out of {} locations",
                successCount, failureCount, activeLocations.size());
    }

    @Transactional
    protected Current fetchAndSaveCurrentWeather(WeatherLocation location) {
        long startTime = System.currentTimeMillis();

        OpenMeteoResponse apiResponse = openMeteoClient.fetchWeatherData(
                location.getLatitude(),
                location.getLongitude()
        );

        long fetchDuration = System.currentTimeMillis() - startTime;

        WeatherCurrent weatherCurrent = weatherMapper.toCurrentEntity(apiResponse, location);
        weatherCurrent.setFetchLatencyMs((int) fetchDuration);

        weatherCurrent = currentRepository.save(weatherCurrent);

        alertService.processWeatherDataForAlerts(weatherCurrent);

        return weatherMapper.toCurrentDTO(weatherCurrent, false);
    }

    @Transactional
    protected List<Forecast> fetchAndSaveForecast(
            WeatherLocation location,
            int days) {

        OpenMeteoResponse apiResponse = openMeteoClient.fetchWeatherData(
                location.getLatitude(),
                location.getLongitude()
        );

        List<WeatherForecast> forecasts = weatherMapper.toForecastEntities(apiResponse, location);

        forecasts = forecastRepository.saveAll(forecasts);

        return forecasts.stream()
                .filter(f -> f.getForecastHour() == null)
                .limit(days)
                .map(weatherMapper::toForecastDTO)
                .toList();
    }

    private Current getFallbackCurrentWeather(WeatherLocation location) {
        return currentRepository
                .findTopByLocationOrderByTimestampDesc(location)
                .map(entity -> weatherMapper.toCurrentDTO(entity, false))
                .orElseThrow(() -> new WeatherApiException(
                        "No weather data available for location: " + location.getId()));
    }

    private List<Forecast> getFallbackForecast(WeatherLocation location, int days) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        return forecastRepository
                .findByLocationAndForecastDateBetweenAndForecastHourIsNull(
                        location, startDate, endDate)
                .stream()
                .map(weatherMapper::toForecastDTO)
                .toList();
    }


    private WeatherLocation findLocationOrThrow(UUID locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));
    }

    private void validateForecastDays(int days) {
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException(
                    "Forecast days must be between 1 and 7, got: " + days);
        }
    }

    private BigDecimal calculateAverageTemperature(List<WeatherCurrent> data) {
        if (data.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = data.stream()
                .map(WeatherCurrent::getTemperature)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(
                BigDecimal.valueOf(data.size()),
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateTotalRainfall(List<WeatherCurrent> data) {
        return data.stream()
                .map(WeatherCurrent::getRainfall)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateWaterStressIndex(BigDecimal rainfall7d, BigDecimal avgTemp) {
        BigDecimal rainfallFactor = BigDecimal.ONE.subtract(
                rainfall7d.divide(RAINFALL_THRESHOLD, 2, RoundingMode.HALF_UP)
                        .min(BigDecimal.ONE)
        );

        BigDecimal tempFactor = avgTemp.subtract(TEMP_BASELINE)
                .divide(TEMP_RANGE, 2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.ONE);

        return rainfallFactor.add(tempFactor)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    private String getWaterStressLevel(BigDecimal index) {
        if (index.compareTo(STRESS_THRESHOLD_LOW) < 0) return "LOW";
        if (index.compareTo(STRESS_THRESHOLD_MEDIUM) < 0) return "MEDIUM";
        if (index.compareTo(STRESS_THRESHOLD_HIGH) < 0) return "HIGH";
        return "CRITICAL";
    }

    private Statistics buildEmptyStatistics() {
        return Statistics.builder()
                .avgTemperatureLast7Days(BigDecimal.ZERO)
                .totalRainfallLast7Days(BigDecimal.ZERO)
                .totalRainfallLast30Days(BigDecimal.ZERO)
                .totalAlerts(0)
                .criticalAlerts(0)
                .waterStressIndex(BigDecimal.ZERO)
                .waterStressLevel("UNKNOWN")
                .build();
    }
}