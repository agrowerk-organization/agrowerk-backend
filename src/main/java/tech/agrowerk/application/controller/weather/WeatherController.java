package tech.agrowerk.application.controller.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.weather.Current;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.application.dto.weather.Dashboard;
import tech.agrowerk.application.dto.weather.Statistics;
import tech.agrowerk.business.service.weather.OpenMeteoClient;
import tech.agrowerk.business.service.weather.WeatherService;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController implements WeatherApi {

    private final WeatherService weatherService;
    private final OpenMeteoClient openMeteoClient;

    @Override
    @GetMapping("/current/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Current> getCurrentWeather(@PathVariable UUID locationId) {
        log.info("Request: Current weather for location {}", locationId);
        return ResponseEntity.ok(weatherService.getCurrentWeather(locationId));
    }

    @Override
    @GetMapping("/forecast/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Forecast>> getForecast(
            @PathVariable UUID locationId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("Request: Forecast for location {} (days: {})", locationId, days);
        return ResponseEntity.ok(weatherService.getForecast(locationId, days));
    }

    @Override
    @GetMapping("/alerts/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlerts(@PathVariable UUID locationId) {
        return ResponseEntity.ok(weatherService.getActiveAlerts(locationId));
    }

    @Override
    @GetMapping("/dashboard/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Dashboard> getDashboard(@PathVariable UUID locationId) {
        return ResponseEntity.ok(weatherService.getDashboard(locationId));
    }

    @Override
    @GetMapping("/statistics/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Statistics> getStatistics(@PathVariable UUID locationId) {
        return ResponseEntity.ok(weatherService.calculateStatistics(locationId));
    }

    @Override
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        String state = openMeteoClient.getCircuitBreakerState();
        var metrics = openMeteoClient.getMetrics();

        Map<String, Object> health = Map.of(
                "status", state.equals("CLOSED") ? "UP" : "DEGRADED",
                "circuitBreaker", Map.of("state", state, "failureRate", metrics.failureRate()),
                "timestamp", Instant.now()
        );

        return ResponseEntity.status(state.equals("CLOSED") ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(health);
    }

    @Override
    @PostMapping("/refresh/{locationId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> forceRefresh(@PathVariable UUID locationId) {
        weatherService.getCurrentWeather(locationId);
        return ResponseEntity.ok(Map.of(
                "message", "Weather data refreshed",
                "timestamp", Instant.now().toString()
        ));
    }
}
