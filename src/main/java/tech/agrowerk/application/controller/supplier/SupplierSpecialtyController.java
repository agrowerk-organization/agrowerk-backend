package tech.agrowerk.application.controller.supplier;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierSpecialtyRequest;
import tech.agrowerk.application.dto.response.supplier.SupplierSpecialtyResponse;
import tech.agrowerk.business.service.supplier.SupplierSpecialtyService;

import java.util.UUID;

@RestController
@RequestMapping("/supplier-specialties")
public class SupplierSpecialtyController {
    private final SupplierSpecialtyService supplierSpecialtyService;

    public SupplierSpecialtyController(SupplierSpecialtyService supplierSpecialtyService) {
        this.supplierSpecialtyService = supplierSpecialtyService;
    }

    @PostMapping("/catalog")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<SupplierSpecialtyResponse> createSpecialty(
            @Valid @RequestBody CreateSupplierSpecialtyRequest request
            ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierSpecialtyService.createSpecialty(request));
    }

    @PostMapping("/add-specialty/{supplierId}/{specialtyId}")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<SupplierSpecialtyResponse> addSpecialty(
            @PathVariable UUID supplierId, @PathVariable UUID specialtyId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierSpecialtyService.addToSupplier(supplierId, specialtyId));
    }

    @GetMapping("/list-catalog")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SupplierSpecialtyResponse>> listCatalog(@PageableDefault(size = 10)Pageable pageable) {
        return ResponseEntity.ok(supplierSpecialtyService.listCatalog(pageable));
    }

    @PatchMapping("/toggle-specialty/{supplierId}/{specialtyId}")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<SupplierSpecialtyResponse> toggleSpecialty(
            @PathVariable UUID supplierId, @PathVariable UUID specialtyId
    ) {
        return ResponseEntity.ok(supplierSpecialtyService.toggleLinkActive(supplierId, specialtyId));
    }

    @DeleteMapping("/remove-specialty/{supplierId}/{specialtyId}")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<Void> removeSpecialty(@PathVariable UUID supplierId, @PathVariable UUID specialtyId) {
        supplierSpecialtyService.removeFromSupplier(supplierId, specialtyId);
        return ResponseEntity.noContent().build();
    }
}
