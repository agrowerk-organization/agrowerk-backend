package tech.agrowerk.application.controller.weather;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationUpdateRequest;
import tech.agrowerk.business.service.weather.WeatherLocationService;

import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/weather/locations")
@RequiredArgsConstructor
@Slf4j
public class WeatherLocationController implements WeatherLocationApi {

    private final WeatherLocationService locationService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<WeatherLocationDto>> getAllLocations(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {

        log.info("GET /weather/locations (activeOnly={})", activeOnly);

        List<WeatherLocationDto> locations = activeOnly
                ? locationService.findActiveLocations()
                : locationService.findAllLocations();

        return ResponseEntity.ok(locations);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> getLocationById(
            @PathVariable UUID id) {

        log.info("GET /weather/locations/{}", id);

        WeatherLocationDto location = locationService.findById(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> getLocationByProperty(
            @PathVariable UUID propertyId) {

        log.info("GET /weather/locations/property/{}", propertyId);

        WeatherLocationDto location = locationService.findByPropertyId(propertyId);
        return ResponseEntity.ok(location);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> createLocation(
             WeatherLocationCreateRequest request) {

        log.info("POST /weather/locations - name={}, lat={}, lon={}",
                request.name(), request.latitude(), request.longitude());

        WeatherLocationDto created = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody WeatherLocationUpdateRequest request) {

        log.info("PUT /weather/locations/{}", id);

        WeatherLocationDto updated = locationService.updateLocation(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> activateLocation(
            @PathVariable UUID id) {

        log.info("PATCH /weather/locations/{}/activate", id);

        locationService.setActive(id, true);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> deactivateLocation(
            @PathVariable UUID id) {

        log.info("PATCH /weather/locations/{}/deactivate", id);

        locationService.setActive(id, false);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable UUID id) {

        log.warn("DELETE /weather/locations/{} - Admin deletion requested", id);

        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }
}
