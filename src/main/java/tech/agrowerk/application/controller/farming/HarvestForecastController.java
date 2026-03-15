package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateHarvestForecastRequest;
import tech.agrowerk.application.dto.request.farming.UpdateHarvestForecastRequest;
import tech.agrowerk.application.dto.response.farming.HarvestForecastResponse;
import tech.agrowerk.business.service.farming.HarvestForecastService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/harvest-forecasts")
public class HarvestForecastController {

    private final HarvestForecastService forecastService;

    public HarvestForecastController(HarvestForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping("/create-harvest-forecast")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestForecastResponse> createForecast(
            @Valid @RequestBody CreateHarvestForecastRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(forecastService.createForecast(request));
    }

    @PutMapping("update-harvest-forecast/{forecastId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestForecastResponse> updateForecast(
            @PathVariable UUID forecastId,
            @Valid @RequestBody UpdateHarvestForecastRequest request) {
        return ResponseEntity.ok(forecastService.updateForecast(forecastId, request));
    }

    @GetMapping("/find-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<HarvestForecastResponse>> findByPlanting(
            @PathVariable UUID plantingId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(forecastService.findByPlanting(plantingId, pageable));
    }

    @GetMapping("/find-by-property/{propertyId}/season/{seasonId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<HarvestForecastResponse>> findByPropertyAndSeason(
            @PathVariable UUID propertyId,
            @PathVariable UUID seasonId,
            @PageableDefault(size = 10)Pageable pageable) {
        return ResponseEntity.ok(
                forecastService.findByPropertyAndSeason(propertyId, seasonId, pageable));
    }

    @GetMapping("/planting/{plantingId}/date/{forecastDate}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestForecastResponse> findByPlantingAndForecastDate(
            @PathVariable UUID plantingId,
            @PathVariable LocalDate forecastDate) {
        return ResponseEntity.ok(
                forecastService.findByPlantingAndForecastDate(plantingId, forecastDate));
    }

    @GetMapping("/property/{propertyId}/season/{seasonId}/crop/{cropId}/latest")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<HarvestForecastResponse>> findLatestByCropAndSeason(
            @PathVariable UUID propertyId,
            @PathVariable UUID seasonId,
            @PathVariable UUID cropId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                forecastService.findLatestByCropAndSeason(propertyId, seasonId, cropId, pageable));
    }}