package tech.agrowerk.application.controller.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.business.service.weather.WeatherAlertService;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/weather/alerts")
@RequiredArgsConstructor
@Slf4j
public class WeatherAlertController {

    private final WeatherAlertService alertService;
    private final WeatherLocationRepository locationRepository;

    @GetMapping("/location/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlertsByLocation(
            @PathVariable UUID locationId) {

        log.info("GET /weather/alerts/location/{}", locationId);

        WeatherLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));

        List<Alert> alerts = alertService.getActiveAlertsByLocation(location);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Integer> getPendingNotifications() {
        log.info("GET /weather/alerts/pending");

        int count = alertService.getPendingNotifications().size();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlertStatistics(
            @PathVariable UUID locationId) {

        log.info("GET /weather/alerts/statistics/{}", locationId);

        WeatherLocation location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found: " + locationId));

        Map<String, Object> stats = alertService.getAlertStatistics(location);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> resolveAlert(
            @PathVariable UUID alertId,

            Authentication authentication) {

        String username = authentication.getName();
        log.info("POST /weather/alerts/{}/resolve - by user: {}", alertId, username);

        alertService.resolveAlert(alertId, username);

        return ResponseEntity.ok(Map.of(
                "message", "Alert resolved successfully",
                "alertId", alertId.toString(),
                "resolvedBy", username,
                "timestamp", Instant.now().toString()
        ));
    }
}