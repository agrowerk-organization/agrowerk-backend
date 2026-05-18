package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateFieldRequest;
import tech.agrowerk.application.dto.request.farming.UpdateFieldRequest;
import tech.agrowerk.application.dto.response.farming.FieldResponse;
import tech.agrowerk.application.dto.response.farming.PlantingResponse;
import tech.agrowerk.business.service.farming.FieldService;
import tech.agrowerk.business.service.farming.PlantingService;

import java.util.UUID;

@RestController
@RequestMapping("/fields")
public class FieldController {

    private final FieldService fieldService;
    private final PlantingService plantingService;

    public FieldController(FieldService fieldService, PlantingService plantingService) {
        this.fieldService = fieldService;
        this.plantingService = plantingService;
    }

    @PostMapping("/create-field")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<FieldResponse> create(@Valid @RequestBody CreateFieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fieldService.createField(request));
    }

    @GetMapping("/find-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<FieldResponse>> findByProperty(
            @PathVariable UUID propertyId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(fieldService.findByProperty(propertyId, pageable));
    }

    @GetMapping("find-by-id/{fieldId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<FieldResponse> findById(@PathVariable UUID fieldId) {
        return ResponseEntity.ok(fieldService.findById(fieldId));
    }

    @PatchMapping("update-field/{fieldId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<FieldResponse> update(
            @PathVariable UUID fieldId,
            @Valid @RequestBody UpdateFieldRequest request) {
        return ResponseEntity.ok(fieldService.updateField(fieldId, request));
    }
}
