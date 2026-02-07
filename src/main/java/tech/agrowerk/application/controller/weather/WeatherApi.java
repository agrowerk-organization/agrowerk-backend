package tech.agrowerk.application.controller.weather;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.weather.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Weather", description = "API for meteorological data and weather forecasts")
public interface WeatherApi {

    @Operation(
            summary = "Get current weather",
            description = "Returns current meteorological data for the specified location. Data is cached for 5 minutes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Weather data retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Current.class))),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "503", description = "Weather service temporarily unavailable")
    })
    ResponseEntity<Current> getCurrentWeather(
            @Parameter(description = "Unique identifier of the location", required = true) UUID locationId);

    @Operation(
            summary = "Get weather forecast",
            description = "Returns meteorological forecast for the next few days (range: 1-7 days)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Forecast retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Forecast.class))),
            @ApiResponse(responseCode = "400", description = "Invalid 'days' parameter (must be between 1 and 7)")
    })
    ResponseEntity<List<Forecast>> getForecast(
            @Parameter(description = "Unique identifier of the location") UUID locationId,
            @Min(1) @Max(7) int days);

    @Operation(
            summary = "Get active weather alerts",
            description = "Returns all currently active weather alerts for the specified location"
    )
    @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully")
    ResponseEntity<List<Alert>> getActiveAlerts(UUID locationId);

    @Operation(
            summary = "Get complete weather dashboard",
            description = "Returns current data, 7-day forecast, active alerts, and statistics in a single optimized endpoint"
    )
    @ApiResponse(responseCode = "200", description = "Dashboard retrieved successfully",
            content = @Content(schema = @Schema(implementation = Dashboard.class)))
    ResponseEntity<Dashboard> getDashboard(UUID locationId);

    @Operation(
            summary = "Get weather statistics",
            description = "Returns historical averages, weather trends, and agricultural indices (e.g., water stress)"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    ResponseEntity<Statistics> getStatistics(UUID locationId);

    @Operation(
            summary = "Health check",
            description = "Checks the status of the weather service and the external API (Open-Meteo)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service is operational"),
            @ApiResponse(responseCode = "503", description = "Service unavailable (Circuit Breaker is OPEN)")
    })
    ResponseEntity<Map<String, Object>> healthCheck();

    @Operation(
            summary = "Force data refresh",
            description = "Bypasses cache and forces a fresh data fetch from the external provider. Requires SYSTEM_ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Data refreshed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Insufficient privileges")
    })
    ResponseEntity<Map<String, String>> forceRefresh(UUID locationId);
}