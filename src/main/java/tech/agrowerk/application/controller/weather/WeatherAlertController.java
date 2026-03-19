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
import tech.agrowerk.business.service.weather.WeatherLocationService;
import tech.agrowerk.business.service.weather.WeatherService;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.repository.weather.WeatherLocationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/weather-alerts")
@Slf4j
public class WeatherAlertController {

    private final WeatherAlertService alertService;
    private final WeatherLocationService locationService;

    public WeatherAlertController(WeatherAlertService alertService, WeatherLocationService locationService) {
        this.alertService = alertService;
        this.locationService = locationService;
    }

    @GetMapping("/get-active/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlertsByLocation(
            @PathVariable UUID locationId) {

        log.info("GET /weather/alerts/location/{}", locationId);

        List<Alert> alerts = alertService.getActiveAlertsByLocation(locationId);
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/get-pending")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Integer> getPendingNotifications() {

        int count = alertService.getPendingNotifications().size();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/get-statistics/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlertStatistics(
            @PathVariable UUID locationId) {

        Map<String, Object> stats = alertService.getAlertStatistics(locationId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("resolve/{alertId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> resolveAlert(
            @PathVariable UUID alertId,

            Authentication authentication) {

        String username = authentication.getName();

        alertService.resolveAlert(alertId, username);

        return ResponseEntity.ok(Map.of(
                "message", "Alert resolved successfully",
                "alertId", alertId.toString(),
                "resolvedBy", username,
                "timestamp", Instant.now().toString()
        ));
    }
}