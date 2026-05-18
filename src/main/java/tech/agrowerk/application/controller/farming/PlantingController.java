package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreatePlantingRequest;
import tech.agrowerk.application.dto.request.farming.UpdatePlantingRequest;
import tech.agrowerk.application.dto.response.farming.PlantingResponse;
import tech.agrowerk.business.service.farming.PlantingService;

import java.util.UUID;

@RestController
@RequestMapping("/plantings")
public class PlantingController {

    private final PlantingService plantingService;

    public PlantingController(PlantingService plantingService) {
        this.plantingService = plantingService;
    }

    @PostMapping("/create-planting")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PlantingResponse> create(
            @Valid @RequestBody CreatePlantingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plantingService.createPlanting(request));
    }

    @GetMapping("/find-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<PlantingResponse>> findByProperty(
            @PathVariable UUID propertyId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(plantingService.findByProperty(propertyId, pageable));
    }

    @GetMapping("find-by-field/{fieldId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<PlantingResponse>> findByField(
            @PathVariable UUID fieldId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(plantingService.findByField(fieldId, PageRequest.of(page, size)));
    }


    @GetMapping("/find-by-id/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PlantingResponse> findById(@PathVariable UUID plantingId) {
        return ResponseEntity.ok(plantingService.findById(plantingId));
    }

    @PutMapping("/update-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PlantingResponse> updatePlanting(@PathVariable UUID plantingId,
                                                           @Valid @RequestBody UpdatePlantingRequest request) {
        return ResponseEntity.ok(plantingService.updatePlanting(plantingId, request));
    }

    @PatchMapping("/cancel-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PlantingResponse> cancel(@PathVariable UUID plantingId) {
        return ResponseEntity.ok(plantingService.cancelPlanting(plantingId));
    }
}