package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.business.mapper.weather.WeatherMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherForecastRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WeatherForecastService {

    private final WeatherForecastRepository forecastRepository;
    private final WeatherLocationRepository locationRepository;
    private final WeatherMapper weatherMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    private static final int MAX_FORECAST_DAYS = 7;

    public WeatherForecastService(
            WeatherForecastRepository forecastRepository,
            WeatherLocationRepository locationRepository,
            WeatherMapper weatherMapper,
            OwnershipValidator ownershipValidator,
            AuthUtil authUtil
    ) {
        this.forecastRepository = forecastRepository;
        this.locationRepository = locationRepository;
        this.weatherMapper = weatherMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Cacheable(value = "weatherForecast", key = "'daily_' + #locationId + '_' + #days")
    @Transactional(readOnly = true)
    public List<Forecast> getDailyForecast(UUID locationId, int days) {
        validateForecastDays(days);

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        WeatherLocation location = findLocationOrThrow(locationId);

        ownershipValidator.validateLocationAccess(location, auth.id());

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(days);

        return forecastRepository
                .findByLocationAndForecastDateBetweenAndForecastHourIsNull(location, start, end)
                .stream()
                .limit(days)
                .map(weatherMapper::toForecastDTO)
                .toList();
    }

    @Cacheable(value = "weatherForecast", key = "'hourly_' + #locationId + '_' + #date")
    @Transactional(readOnly = true)
    public List<Forecast> getHourlyForecast(UUID locationId, LocalDate date) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        WeatherLocation location = findLocationOrThrow(locationId);

        ownershipValidator.validateLocationAccess(location, auth.id());

        return forecastRepository
                .findByLocationAndForecastDateAndForecastHourIsNotNull(location, date)
                .stream()
                .map(weatherMapper::toForecastDTO)
                .toList();
    }

    @Cacheable(value = "weatherForecast", key = "'history_' + #locationId + '_' + #pageable")
    @Transactional(readOnly = true)
    public Page<Forecast> getForecastHistory(UUID locationId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        WeatherLocation location = findLocationOrThrow(locationId);

        ownershipValidator.validateLocationAccess(location, auth.id());

        log.debug("Fetching forecast history for location: {} - Cache MISS", locationId);

        return forecastRepository
                .findByLocationAndForecastHourIsNullOrderByForecastDateDesc(location, pageable)
                .map(weatherMapper::toForecastDTO);
    }

    @Cacheable(value = "weatherForecast", key = "'user_' + #auth.id() + '_' + #start + '_' + #end + '_' + #pageable")
    @Transactional(readOnly = true)
    public Page<Forecast> getForecastsByUser(LocalDate start, LocalDate end, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        validateDateRange(start, end);

        log.debug("Fetching forecasts for user: {} between {} and {} - Cache MISS",
                auth.id(), start, end);

        return forecastRepository
                .findDailyForecastsByUserId(auth.id(), start, end, pageable)
                .map(weatherMapper::toForecastDTO);
    }

    @CacheEvict(value = "weatherForecast", allEntries = true)
    @Transactional
    public void deleteForecastsForLocation(UUID locationId, LocalDate start, LocalDate end) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        WeatherLocation location = findLocationOrThrow(locationId);

        ownershipValidator.validateLocationAccess(location, auth.id());

        log.info("Deleting forecasts for location: {} between {} and {}. User: {}",
                locationId, start, end, auth.id());

        forecastRepository.deleteByLocationAndForecastDateBetween(location, start, end);
    }


    private WeatherLocation findLocationOrThrow(UUID id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));
    }


    private void validateForecastDays(int days) {
        if (days < 1 || days > MAX_FORECAST_DAYS)
            throw new IllegalArgumentException(
                    "Forecast days must be between 1 and " + MAX_FORECAST_DAYS + ".");
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start))
            throw new IllegalArgumentException("Invalid date range: start must be before end.");
    }
}