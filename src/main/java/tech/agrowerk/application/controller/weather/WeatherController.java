package tech.agrowerk.application.controller.weather;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.open_meteo.OpenMeteoResponse;
import tech.agrowerk.application.dto.weather.Current;
import tech.agrowerk.application.dto.weather.Forecast;
import tech.agrowerk.application.dto.weather.Alert;
import tech.agrowerk.application.dto.weather.Dashboard;
import tech.agrowerk.application.dto.weather.Statistics;
import tech.agrowerk.infrastructure.client.OpenMeteoClient;
import tech.agrowerk.business.service.weather.WeatherCacheService;
import tech.agrowerk.business.service.weather.WeatherDashboardService;
import tech.agrowerk.business.service.weather.WeatherService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/weather")
@Slf4j
public class WeatherController {

    private final WeatherCacheService cacheService;
    private final WeatherDashboardService dashboardService;
    private final WeatherService weatherService;
    private final OpenMeteoClient openMeteoClient;

    public WeatherController(WeatherCacheService cacheService, WeatherDashboardService dashboardService, WeatherService weatherService, OpenMeteoClient openMeteoClient) {
        this.cacheService = cacheService;
        this.dashboardService = dashboardService;
        this.weatherService = weatherService;
        this.openMeteoClient = openMeteoClient;
    }

    @GetMapping("/get-current/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Current> getCurrentWeather(@PathVariable UUID locationId) {

        return ResponseEntity.ok(cacheService.getCurrentWeather(locationId));
    }

    @GetMapping("/get-forecast/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Forecast>> getForecast(
            @PathVariable UUID locationId,
            @RequestParam(defaultValue = "7") int days
    ) {

        return ResponseEntity.ok(cacheService.getForecast(locationId, days));
    }


    @GetMapping("/get-alerts/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlerts(@PathVariable UUID locationId) {

        return ResponseEntity.ok(cacheService.getActiveAlerts(locationId));
    }

    @GetMapping("/get-dashboard/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Dashboard> getDashboard(@PathVariable UUID locationId) {

        return ResponseEntity.ok(dashboardService.getDashboard(locationId));
    }


    @GetMapping("/get-statistics/{locationId}")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN', 'SYSTEM_ADMIN')")
    public ResponseEntity<Statistics> getStatistics(@PathVariable UUID locationId) {

        return ResponseEntity.ok(cacheService.calculateStatistics(locationId));
    }

    @GetMapping("/get-health")
    public ResponseEntity<Map<String, Object>> healthCheck() {

        String state = openMeteoClient.getCircuitBreakerState();
        var metrics = openMeteoClient.getMetrics();

        Map<String, Object> health = Map.of(
                "status", state.equals("CLOSED") ? "UP" : "DEGRADED",
                "circuitBreaker", Map.of(
                        "state", state,
                        "failureRate", metrics.failureRate()
                ),
                "timestamp", Instant.now()
        );

        HttpStatus status =
                state.equals("CLOSED")
                        ? HttpStatus.OK
                        : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(status).body(health);
    }

    @GetMapping("/test-circuit")
    public ResponseEntity<OpenMeteoResponse> testCircuit() {
        return ResponseEntity.ok(openMeteoClient.fetchWeatherData(
                new BigDecimal("-7.1895"),
                new BigDecimal("-39.3328")
        ));
    }

    @PostMapping("/refresh/{locationId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, String>> forceRefresh(@PathVariable UUID locationId) {

        weatherService.getCurrentWeatherInternal(locationId);

        return ResponseEntity.ok(Map.of(
                "message", "Weather data refreshed",
                "timestamp", Instant.now().toString()
        ));
    }
}
