package tech.agrowerk.application.controller.supplier;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierRequest;
import tech.agrowerk.application.dto.request.supplier.UpdateSupplierRequest;
import tech.agrowerk.application.dto.response.property.PropertyResponse;
import tech.agrowerk.application.dto.response.supplier.SupplierResponse;
import tech.agrowerk.business.service.supplier.SupplierService;

import java.util.UUID;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }


    @PostMapping("/create-supplier")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @GetMapping("/find-by-id/{supplierId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupplierResponse> findById(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierService.findById(supplierId));
    }

    @GetMapping("/find-by-cnpj/{cnpj}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SupplierResponse> findByCnpj(@PathVariable String cnpj) {
        return ResponseEntity.ok(supplierService.findByCnpj(cnpj));
    }

    @GetMapping("/get-me")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<SupplierResponse> getMySupplier() {
        return ResponseEntity.ok(supplierService.getMySupplier());
    }

    @GetMapping("/list-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SupplierResponse>> listAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supplierService.listAll(pageable));
    }

    @GetMapping("/list-by-state")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<SupplierResponse>> listByState(
            @RequestParam String state, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supplierService.listByState(state, pageable));
    }

    @PutMapping("/update-me")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<SupplierResponse> updateSupplier(@Valid @RequestBody UpdateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(request));
    }

    @PatchMapping("/toggle-active/{id}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id) {
        supplierService.toggleActive(id);
        return ResponseEntity.noContent().build();
    }
}
