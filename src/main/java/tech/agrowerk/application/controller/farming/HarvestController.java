package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateHarvestRequest;
import tech.agrowerk.application.dto.response.farming.HarvestResponse;
import tech.agrowerk.business.service.farming.HarvestService;

import java.util.UUID;

@RestController
@RequestMapping("/harvests")
public class HarvestController {

    private final HarvestService harvestService;

    public HarvestController(HarvestService harvestService) {
        this.harvestService = harvestService;
    }

    @PostMapping("/create-harvest")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestResponse> create(
            @Valid @RequestBody CreateHarvestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestService.createHarvest(request));
    }

    @PatchMapping("finalize-harvest/{harvestId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestResponse> finalize(
            @PathVariable UUID harvestId) {
        return ResponseEntity.ok(
                harvestService.finalizeHarvest(harvestId));
    }

    @GetMapping("/find-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestResponse> findByPlanting(
            @PathVariable UUID plantingId) {
        return ResponseEntity.ok(
                harvestService.findByPlanting(plantingId));
    }

    @GetMapping("/find-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<HarvestResponse>> findByProperty(
            @PathVariable UUID propertyId, Pageable pageable) {
        return ResponseEntity.ok(
                harvestService.findByProperty(propertyId, pageable));
    }
}
