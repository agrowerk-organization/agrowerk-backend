package tech.agrowerk.application.controller.monitoring;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.weather.CircuitBreakerMetrics;
import tech.agrowerk.infrastructure.client.OpenMeteoClient;

@RestController
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController implements MonitoringApi {

    private final OpenMeteoClient openMeteoClient;

    @Override
    @GetMapping("/circuit-breaker/state")
    public ResponseEntity<String> getCircuitBreakerState() {
        return ResponseEntity.ok(openMeteoClient.getCircuitBreakerState());
    }

    @Override
    @GetMapping("/circuit-breaker/metrics")
    public ResponseEntity<CircuitBreakerMetrics> getMetrics() {
        return ResponseEntity.ok(openMeteoClient.getMetrics());
    }

    @Override
    @PostMapping("/circuit-breaker/reset")
    public ResponseEntity<String> resetCircuitBreaker() {
        openMeteoClient.resetCircuitBreaker();
        return ResponseEntity.ok("Circuit Breaker reset");
    }

    @Override
    @PostMapping("/circuit-breaker/force-open")
    public ResponseEntity<String> forceOpen() {
        openMeteoClient.forceOpen();
        return ResponseEntity.ok("Circuit Breaker forced open");
    }
}