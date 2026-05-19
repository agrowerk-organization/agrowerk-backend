package tech.agrowerk.application.controller.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.inventory.CreateWarehouseRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateWarehouseRequest;
import tech.agrowerk.application.dto.response.inventory.WarehouseResponse;
import tech.agrowerk.business.service.inventory.WarehouseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/create-warehouse")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseService.createWarehouse(request));
    }

    @PatchMapping("update-warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<WarehouseResponse> update(
            @PathVariable UUID warehouseId,
            @Valid @RequestBody UpdateWarehouseRequest request) {
        return ResponseEntity.ok(
                warehouseService.updateWarehouse(warehouseId, request));
    }

    @PatchMapping("deactivate-warehouse/{warehouseId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> deactivateWarehouse(
            @PathVariable UUID warehouseId) {
        warehouseService.deactivateWarehouse(warehouseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<WarehouseResponse>> findByProperty(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(
                warehouseService.findByProperty(propertyId));
    }

    @GetMapping("/find-by-id/{warehouseId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<WarehouseResponse> findById(
            @PathVariable UUID warehouseId) {
        return ResponseEntity.ok(
                warehouseService.findById(warehouseId));
    }
}