package tech.agrowerk.application.controller.weather;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationUpdateRequest;
import tech.agrowerk.business.service.weather.WeatherLocationService;

import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/weather-locations")
@Slf4j
public class WeatherLocationController {

    private final WeatherLocationService locationService;

    public WeatherLocationController(WeatherLocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/get-all-locations")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<WeatherLocationDto>> getAllLocations(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly) {

        List<WeatherLocationDto> locations = activeOnly
                ? locationService.findActiveLocations()
                : locationService.findAllLocations();

        return ResponseEntity.ok(locations);
    }

    @GetMapping("/find-by-id/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> getLocationById(
            @PathVariable UUID id) {

        WeatherLocationDto location = locationService.findById(id);
        return ResponseEntity.ok(location);
    }

    @GetMapping("/get-by-property/{propertyId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> getLocationByProperty(
            @PathVariable UUID propertyId) {

        WeatherLocationDto location = locationService.findByPropertyId(propertyId);
        return ResponseEntity.ok(location);
    }

    @PostMapping("/create-location")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> createLocation(
             WeatherLocationCreateRequest request) {

        log.info("POST /weather/locations - name={}, lat={}, lon={}",
                request.name(), request.latitude(), request.longitude());

        WeatherLocationDto created = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/update-location/{id}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<WeatherLocationDto> updateLocation(
            @PathVariable UUID id,
            @Valid @RequestBody WeatherLocationUpdateRequest request) {

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

    @PutMapping("/deactivate-location/{locationId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> deactivateLocation(@PathVariable UUID locationId) {

        locationService.setActive(locationId, false);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-location/{locationId}")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable UUID locationId) {

        locationService.deleteLocation(locationId);
        return ResponseEntity.noContent().build();
    }
}
