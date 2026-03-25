package tech.agrowerk.application.controller.supplier;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierRatingRequest;
import tech.agrowerk.application.dto.response.supplier.SupplierRatingResponse;
import tech.agrowerk.business.service.supplier.SupplierRatingService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/supplier-ratings")
public class SupplierRatingController {
    private final SupplierRatingService supplierRatingService;

    public SupplierRatingController(SupplierRatingService supplierRatingService) {
        this.supplierRatingService = supplierRatingService;
    }

    @PostMapping("/rate")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SupplierRatingResponse> rateSupplier(@Valid @RequestBody CreateSupplierRatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierRatingService.rateSupplier(request));
    }

    @GetMapping("/rating/{supplierId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> getAverageRating(@PathVariable UUID supplierId) {
        return ResponseEntity.ok(supplierRatingService.getAverageRating(supplierId));
    }
}
