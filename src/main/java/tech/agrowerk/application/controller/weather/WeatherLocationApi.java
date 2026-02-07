package tech.agrowerk.application.controller.weather;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import tech.agrowerk.application.dto.weather.location.WeatherLocationCreateRequest;
import tech.agrowerk.application.dto.weather.location.WeatherLocationDto;
import tech.agrowerk.application.dto.weather.location.WeatherLocationUpdateRequest;

import java.util.List;
import java.util.UUID;

@Tag(name = "Weather Locations", description = "Management of geographic locations for meteorological monitoring")
public interface WeatherLocationApi {

    @Operation(summary = "List locations", description = "Returns all registered locations for weather monitoring.")
    @ApiResponse(responseCode = "200", description = "List of locations retrieved successfully")
    ResponseEntity<List<WeatherLocationDto>> getAllLocations(
            @Parameter(description = "Filter only active locations") Boolean activeOnly);

    @Operation(summary = "Get location by ID", description = "Returns details of a specific weather location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    ResponseEntity<WeatherLocationDto> getLocationById(@Parameter(description = "Location unique ID") UUID id);

    @Operation(summary = "Get location by property", description = "Returns the weather location linked to a rural property.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location found"),
            @ApiResponse(responseCode = "404", description = "Property has no linked weather location")
    })
    ResponseEntity<WeatherLocationDto> getLocationByProperty(@Parameter(description = "Property unique ID") UUID propertyId);

    @Operation(summary = "Create location", description = "Registers a new location for monitoring coordinates.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Location created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Location already exists for these coordinates")
    })
    ResponseEntity<WeatherLocationDto> createLocation(@Valid @RequestBody WeatherLocationCreateRequest request);

    @Operation(summary = "Update location", description = "Updates details for an existing weather location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "404", description = "Location not found")
    })
    ResponseEntity<WeatherLocationDto> updateLocation(UUID id, WeatherLocationUpdateRequest request);

    @Operation(summary = "Activate location", description = "Enables weather monitoring for the location.")
    @ApiResponse(responseCode = "200", description = "Location activated successfully")
    ResponseEntity<Void> activateLocation(UUID id);

    @Operation(summary = "Deactivate location", description = "Disables weather monitoring for the location.")
    @ApiResponse(responseCode = "200", description = "Location deactivated successfully")
    ResponseEntity<Void> deactivateLocation(UUID id);

    @Operation(summary = "Remove location", description = "Permanently removes a location and its associated weather data. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Location removed successfully"),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    ResponseEntity<Void> deleteLocation(UUID id);
}