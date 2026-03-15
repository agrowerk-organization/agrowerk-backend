package tech.agrowerk.business.service.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;
import tech.agrowerk.application.dto.weather.location.WeatherLocationUpdateRequest;
import tech.agrowerk.business.mapper.weather.WeatherMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.util.UUID;

@Service
@Slf4j
public class WeatherLocationService {

    private final WeatherLocationRepository locationRepository;
    private final PropertyRepository propertyRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final WeatherMapper weatherMapper;
    private final AuthUtil authUtil;

    public WeatherLocationService(
            WeatherLocationRepository locationRepository,
            PropertyRepository propertyRepository,
            UserPropertyRepository userPropertyRepository,
            WeatherMapper weatherMapper,
            AuthUtil authUtil
    ) {
        this.locationRepository = locationRepository;
        this.propertyRepository = propertyRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.weatherMapper = weatherMapper;
        this.authUtil = authUtil;
    }

    @Cacheable(value = "weatherLocations", key = "'user_' + #auth.id() + '_' + #pageable")
    @Transactional(readOnly = true)
    public Page<WeatherLocationDto> findLocationsByUser(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        log.debug("Finding weather locations for user: {} - Cache MISS", auth.id());

        return locationRepository
                .findAllByUserId(auth.id(), pageable)
                .map(weatherMapper::toLocationDTO);
    }

    @Cacheable(value = "weatherLocations", key = "#id")
    @Transactional(readOnly = true)
    public WeatherLocationDto findById(UUID id) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        verifyPropertyAccess(auth, location);

        return weatherMapper.toLocationDTO(location);
    }

    @Cacheable(value = "weatherLocations", key = "'prop_' + #propertyId")
    @Transactional(readOnly = true)
    public WeatherLocationDto findByPropertyId(UUID propertyId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        log.debug("Finding weather location for property: {} - Cache MISS", propertyId);

        verifyUserHasAccessToProperty(auth, propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found: " + propertyId));

        return locationRepository.findByProperty(property)
                .map(weatherMapper::toLocationDTO)
                .orElseThrow(() -> new EntityNotFoundException("No weather location found for property: " + propertyId));
    }

    @CacheEvict(value = "weatherLocations", allEntries = true)
    @Transactional
    public WeatherLocationDto createLocation(WeatherLocationCreateRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found: " + request.propertyId()));

        verifyUserHasAccessToProperty(auth, property.getId());

        locationRepository.findByProperty(property).ifPresent(existing -> {
            throw new EntityAlreadyExistsException("This property already has a weather location assigned.");
        });

        locationRepository.findByLatitudeAndLongitude(request.latitude(), request.longitude())
                .ifPresent(existing -> {
                    throw new EntityAlreadyExistsException("A location already exists at these coordinates.");
                });

        WeatherLocation location = WeatherLocation.builder()
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .state(request.state())
                .country(request.country())
                .timezone(request.timezone())
                .active(request.active())
                .property(property)
                .build();

        log.info("Creating weather location for property: {}. User: {}", property.getId(), auth.id());
        return weatherMapper.toLocationDTO(locationRepository.save(location));
    }

    @CacheEvict(value = "weatherLocations", allEntries = true)
    @Transactional
    public WeatherLocationDto updateLocation(UUID id, WeatherLocationUpdateRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        verifyPropertyAccess(auth, location);

        if (request.name() != null) location.setName(request.name());
        if (request.timezone() != null) location.setTimezone(request.timezone());
        if (request.active() != null) location.setActive(request.active());

        if (request.propertyId() != null) {
            verifyUserHasAccessToProperty(auth, request.propertyId());
            Property newProperty = propertyRepository.findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException("Property not found: " + request.propertyId()));
            location.setProperty(newProperty);
        }

        log.info("Updating weather location: {}. User: {}", id, auth.id());
        return weatherMapper.toLocationDTO(locationRepository.save(location));
    }

    @CacheEvict(value = "weatherLocations", allEntries = true)
    @Transactional
    public void setActive(UUID id, boolean active) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        verifyPropertyAccess(auth, location);

        location.setActive(active);
        locationRepository.save(location);
        log.info("Changed active status to {} for location: {}. User: {}", active, id, auth.id());
    }

    @CacheEvict(value = "weatherLocations", allEntries = true)
    @Transactional
    public void deleteLocation(UUID id) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        WeatherLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Weather location not found: " + id));

        verifyPropertyAccess(auth, location);

        locationRepository.delete(location);
        log.warn("Deleted weather location: {}. User: {}", id, auth.id());
    }

    private void verifyPropertyAccess(AuthenticatedUser auth, WeatherLocation location) {
        if (location.getProperty() == null) {
            log.warn("Access denied: location {} has no property bound. User: {}", location.getId(), auth.id());
            throw new AccessDeniedException("You do not have permission to access this location.");
        }
        verifyUserHasAccessToProperty(auth, location.getProperty().getId());
    }

    private void verifyUserHasAccessToProperty(AuthenticatedUser auth, UUID propertyId) {
        boolean hasAccess = userPropertyRepository.existsByUserIdAndPropertyId(auth.id(), propertyId);
        if (!hasAccess) {
            log.warn("Access denied: User {} has no access to property {}", auth.id(), propertyId);
            throw new AccessDeniedException("You do not have permission to manage this property.");
        }
    }
}