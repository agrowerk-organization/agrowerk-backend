/*package tech.agrowerk.application.controller.monitoring;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import tech.agrowerk.application.dto.weather.CircuitBreakerMetrics;

@Tag(name = "Monitoring", description = "Endpoints for health checks and resilience monitoring (Circuit Breaker)")
public interface MonitoringApi {

    @Operation(
            summary = "Get Circuit Breaker state",
            description = "Returns the current state of the Weather Service Circuit Breaker (e.g., CLOSED, OPEN, HALF_OPEN)."
    )
    ResponseEntity<String> getCircuitBreakerState();

    @Operation(
            summary = "Get Circuit Breaker metrics",
            description = "Returns detailed metrics such as failure rate, slow call rate, and number of buffered calls."
    )
    ResponseEntity<CircuitBreakerMetrics> getMetrics();

    @Operation(
            summary = "Reset Circuit Breaker",
            description = "Manually resets the Circuit Breaker to its initial CLOSED state, clearing all recorded metrics."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Circuit Breaker reset successfully")
    })
    ResponseEntity<String> resetCircuitBreaker();

    @Operation(
            summary = "Force open Circuit Breaker",
            description = "Manually forces the Circuit Breaker into the OPEN state, rejecting all incoming requests to the Weather Service."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Circuit Breaker forced open successfully")
    })
    ResponseEntity<String> forceOpen();
}*/