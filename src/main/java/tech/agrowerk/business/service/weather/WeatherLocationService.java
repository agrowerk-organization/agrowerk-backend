package tech.agrowerk.business.service.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;
import tech.agrowerk.application.dto.weather.location.WeatherLocationUpdateRequest;
import tech.agrowerk.business.mapper.WeatherMapper;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherLocationService {

    private final WeatherLocationRepository locationRepository;
    private final PropertyRepository propertyRepository;
    private final WeatherMapper weatherMapper;


    @Cacheable(value = "weatherLocations", key = "'all'", cacheManager = "redisCacheManager")    @Transactional(readOnly = true)
    public List<WeatherLocationDto> findAllLocations() {
        log.debug("Finding all weather locations");

        return locationRepository.findAll()
                .stream()
                .map(weatherMapper::toLocationDTO)
                .toList();
    }

    @Cacheable(value = "weatherLocations", key = "'prop_'", cacheManager = "redisCacheManager")    @Transactional(readOnly = true)
    public List<WeatherLocationDto> findActiveLocations() {
        log.debug("Finding active weather locations");

        return locationRepository.findByActiveTrue()
                .stream()
                .map(weatherMapper::toLocationDTO)
                .toList();
    }


    @Cacheable(value = "weatherLocations", key = "#id", cacheManager = "redisCacheManager")    @Transactional(readOnly = true)
    public WeatherLocationDto findById(UUID id) {
        log.debug("Finding weather location by id: {}", id);

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        return weatherMapper.toLocationDTO(location);
    }

    @Cacheable(value = "weatherLocations", key = "'prop_' + #propertyId", cacheManager = "redisCacheManager")
    @Transactional(readOnly = true)
    public WeatherLocationDto findByPropertyId(UUID propertyId) {
        log.debug("Finding weather location by property: {}", propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found: " + propertyId));

        WeatherLocation location = locationRepository.findByProperty(property)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No weather location found for property: " + propertyId));

        return weatherMapper.toLocationDTO(location);
    }

    @Caching(evict = {
            @CacheEvict(value = "weatherLocations", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "weatherLocations", allEntries = true, cacheManager = "redisCacheManager")
    })
    @Transactional
    public WeatherLocationDto createLocation(WeatherLocationCreateRequest request) {
        log.info("Creating weather location: name={}, lat={}, lon={}",
                request.name(), request.latitude(), request.longitude());

        locationRepository.findByLatitudeAndLongitude(request.latitude(), request.longitude())
                .ifPresent(existing -> {
                    throw new EntityAlreadyExistsException(
                            "Weather location already exists for these coordinates");
        });

        WeatherLocation location = WeatherLocation.builder()
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .state(request.state())
                .country(request.country())
                .timezone(request.timezone())
                .active(request.active())
                .build();

        if (request.propertyId() != null) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Property not found: " + request.propertyId()));

            location.setProperty(property);
        }

        location = locationRepository.save(location);

        log.info("Weather location created successfully: id={}", location.getId());

        return weatherMapper.toLocationDTO(location);
    }

    @Caching(evict = {
            @CacheEvict(value = "weatherLocations", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "weatherLocations", allEntries = true, cacheManager = "redisCacheManager")
    })
    @Transactional
    public WeatherLocationDto updateLocation(UUID id, WeatherLocationUpdateRequest request) {
        log.info("Updating weather location: id={}", id);

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        if (request.name() != null) {
            location.setName(request.name());
        }

        if (request.timezone() != null) {
            location.setTimezone(request.timezone());
        }

        if (request.active() != null) {
            location.setActive(request.active());
        }

        if (request.propertyId() != null) {
            Property property = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Property not found: " + request.propertyId()));

            location.setProperty(property);
        }

        location = locationRepository.save(location);

        log.info("Weather location updated successfully: id={}", id);

        return weatherMapper.toLocationDTO(location);
    }

    @Caching(evict = {
            @CacheEvict(value = "weatherLocations", allEntries = true, cacheManager = "caffeineCacheManager"),
            @CacheEvict(value = "weatherLocations", allEntries = true, cacheManager = "redisCacheManager")
    })
    @Transactional
    public void setActive(UUID id, boolean active) {
        log.info("Setting weather location active status: id={}, active={}", id, active);

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        location.setActive(active);
        locationRepository.save(location);

        log.info("Weather location active status updated: id={}, active={}", id, active);
    }


    @CacheEvict(value = "weatherLocations", allEntries = true)
    @Transactional
    public void deleteLocation(UUID id) {
        log.warn("Deleting weather location: id={}", id);

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        locationRepository.delete(location);

        log.warn("Weather location deleted: id={}", id);
    }
}