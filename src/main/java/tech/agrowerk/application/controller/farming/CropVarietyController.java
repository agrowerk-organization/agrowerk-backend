package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.create.CreateCropVarietyRequest;
import tech.agrowerk.application.dto.request.update.UpdateCropVarietyRequest;
import tech.agrowerk.application.dto.response.CropVarietyResponse;
import tech.agrowerk.business.service.farming.CropVarietyService;

import java.util.UUID;

@RestController
@RequestMapping("/crop-varieties")
public class CropVarietyController {

    private final CropVarietyService cropVarietyService;

    public CropVarietyController(CropVarietyService cropVarietyService) {
        this.cropVarietyService = cropVarietyService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<CropVarietyResponse> createCropVariety(
            @Valid @RequestBody CreateCropVarietyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cropVarietyService.createVariety(request));
    }

    @GetMapping("/crop/{cropVarietyId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CropVarietyResponse>> findByCrop(
            @PathVariable UUID cropVarietyId, Pageable pageable) {
        return ResponseEntity.ok(cropVarietyService.findByCrop(cropVarietyId, pageable));
    }

    @GetMapping("/crop/{cropVarietyId}/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CropVarietyResponse>> search(
            @PathVariable UUID cropVarietyId,
            @RequestParam String name,
            Pageable pageable) {
        return ResponseEntity.ok(cropVarietyService.searchByName(cropVarietyId, name, pageable));
    }

    public ResponseEntity<CropVarietyResponse> updateCropVariety(
            @PathVariable UUID cropVarietyId,
            @Valid @RequestBody UpdateCropVarietyRequest request) {
        return ResponseEntity.ok(cropVarietyService.updateCropVariety(cropVarietyId, request));
    }
}
