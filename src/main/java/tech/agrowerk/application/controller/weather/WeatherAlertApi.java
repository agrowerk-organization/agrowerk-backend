package tech.agrowerk.application.controller.weather;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import tech.agrowerk.application.dto.weather.Alert;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Weather Alerts", description = "Management and tracking of meteorological alerts")
public interface WeatherAlertApi {

    @Operation(summary = "List active alerts", description = "Returns all currently active weather alerts for a specific location.")
    @ApiResponse(responseCode = "200", description = "Alert list retrieved successfully")
    ResponseEntity<List<Alert>> getActiveAlertsByLocation(@Parameter(description = "Location unique ID") UUID locationId);

    @Operation(summary = "List pending alerts", description = "Returns count of alerts pending notification. System Admin only.")
    @ApiResponse(responseCode = "200", description = "Pending count retrieved successfully")
    ResponseEntity<Integer> getPendingNotifications();

    @Operation(summary = "Get alert statistics", description = "Returns alert metrics by type and severity for a location.")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    ResponseEntity<Map<String, Object>> getAlertStatistics(UUID locationId);

    @Operation(summary = "Resolve alert", description = "Manually marks an alert as resolved before its natural expiration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    ResponseEntity<Map<String, String>> resolveAlert(UUID alertId, Authentication authentication);
}