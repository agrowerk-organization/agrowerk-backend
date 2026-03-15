package tech.agrowerk.application.controller.property;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.property.AddOwnerRequest;
import tech.agrowerk.application.dto.request.property.CreatePropertyRequest;
import tech.agrowerk.application.dto.request.property.UpdatePropertyRequest;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.application.dto.response.property.PropertyResponse;
import tech.agrowerk.business.service.property.PropertyService;
import tech.agrowerk.infrastructure.model.property.enums.OwnerRemovalReason;

import java.util.UUID;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody CreatePropertyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(propertyService.createProperty(request));
    }

    @GetMapping("/my-properties")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<PropertyResponse>> myProperties(Pageable pageable) {
        return ResponseEntity.ok(propertyService.findMyProperties(pageable));
    }

    @PutMapping("/{propertyId}/update")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PropertyResponse> update(
            @PathVariable UUID propertyId,
            @Valid @RequestBody UpdatePropertyRequest request) {
        return ResponseEntity.ok(propertyService.updateProperty(propertyId, request));
    }

    @PostMapping("/{propertyId}/owners")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> addOwner(
            @PathVariable UUID propertyId,
            @Valid @RequestBody AddOwnerRequest request) {
        propertyService.addOwner(propertyId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{propertyId}/owners/{targetUserId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> removeOwner(
            @PathVariable UUID propertyId,
            @PathVariable UUID targetUserId,
            @RequestParam OwnerRemovalReason reason) {
        propertyService.removeOwner(propertyId, targetUserId, reason);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{propertyId}/owners/{targetUserId}/permissions")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> updateEditPermission(
            @PathVariable UUID propertyId,
            @PathVariable UUID targetUserId,
            @RequestParam boolean canEdit) {
        propertyService.updateEditPermission(propertyId, targetUserId, canEdit);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{propertyId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<FileUploadResponse> uploadPhoto(
            @PathVariable UUID propertyId,
            @RequestParam("file") MultipartFile file) {
        propertyService.uploadPhoto(propertyId, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
