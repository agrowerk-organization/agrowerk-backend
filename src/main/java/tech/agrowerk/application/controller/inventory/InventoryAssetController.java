package tech.agrowerk.application.controller.inventory;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.inventory.CreateInventoryAssetRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateInventoryAssetRequest;
import tech.agrowerk.application.dto.response.inventory.InventoryAssetResponse;
import tech.agrowerk.business.service.inventory.InventoryAssetService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory-assets")
public class InventoryAssetController {

    private final InventoryAssetService assetService;

    public InventoryAssetController(InventoryAssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(value = "/create-asset", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<InventoryAssetResponse> create(@RequestPart("data") @Valid CreateInventoryAssetRequest request,
                                                         @RequestPart("photos") List<MultipartFile> photos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assetService.createAsset(request, photos));
    }

    @PutMapping("/update-asset/{assetId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<InventoryAssetResponse> updateAsset(@PathVariable UUID assetId,
            @Valid @RequestBody UpdateInventoryAssetRequest request) {
        return ResponseEntity.ok(
                assetService.updateAsset(assetId, request));
    }

    @PostMapping(value = "/{assetId}/photos",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<InventoryAssetResponse> addPhotos(
            @PathVariable UUID assetId,
            @RequestPart("photos") List<MultipartFile> photos) {
        return ResponseEntity.ok(
                assetService.addPhotos(assetId, photos));
    }

    @PatchMapping("/{assetId}/request-barter")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<InventoryAssetResponse> requestBarter(
            @PathVariable UUID assetId) {
        return ResponseEntity.ok(
                assetService.requestBarterApproval(assetId));
    }

    @PatchMapping("/{assetId}/approve-barter")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<InventoryAssetResponse> approveBarter(
            @PathVariable UUID assetId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(
                assetService.approveForBarter(assetId, notes));
    }

    @PatchMapping("/{assetId}/reject-barter")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> rejectBarter(
            @PathVariable UUID assetId,
            @RequestParam String reason) {
        assetService.rejectBarterApproval(assetId, reason);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-assets")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<InventoryAssetResponse>> myAssets(Pageable pageable) {
        return ResponseEntity.ok(
                assetService.findMyAssets(pageable));
    }

    @GetMapping("/barter-catalog")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InventoryAssetResponse>> barterCatalog(
            Pageable pageable) {
        return ResponseEntity.ok(
                assetService.findBarterCatalog(pageable));
    }

    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Page<InventoryAssetResponse>> pendingApproval(
            Pageable pageable) {
        return ResponseEntity.ok(
                assetService.findPendingApproval(pageable));
    }
}
