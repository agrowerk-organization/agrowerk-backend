package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.create.CreateCropRequest;
import tech.agrowerk.application.dto.request.update.UpdateCropRequest;
import tech.agrowerk.application.dto.response.CropResponse;
import tech.agrowerk.business.service.farming.CropService;

import java.util.UUID;

@RestController
@RequestMapping("/crops")
public class CropController {
    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping("/create-crop")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<CropResponse> create(@Valid @RequestBody CreateCropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropService.createCrop(request));
    }

    @GetMapping("/list-crops")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CropResponse>> listCrops(Pageable pageable) {
        return ResponseEntity.ok(cropService.listCrops(pageable));
    }

    @GetMapping("/search-crop")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CropResponse>> search(
            @RequestParam String name,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(cropService.searchByName(name, pageable));
    }

    @GetMapping("/find-crop-by-id/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CropResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(cropService.findById(id));
    }

    @PutMapping("/update-crop/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<CropResponse> updateCrop(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCropRequest request) {
        return ResponseEntity.ok(cropService.updateCrop(id, request));
    }
}
