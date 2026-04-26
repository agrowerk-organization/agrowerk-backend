package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateBatchRequest;
import tech.agrowerk.application.dto.request.farming.ReceiveBatchRequest;
import tech.agrowerk.application.dto.response.farming.BatchResponse;
import tech.agrowerk.business.service.farming.BatchService;

import java.util.UUID;

@RestController
@RequestMapping("/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/create-batch")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<BatchResponse> createBatch(
            @Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(batchService.createBatch(request));
    }

    @PatchMapping("/receive-batch/{batchId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BatchResponse> receiveBatch(
            @PathVariable UUID batchId,
            @Valid @RequestBody ReceiveBatchRequest request) {
        return ResponseEntity.ok(batchService.receiveBatch(batchId, request));
    }

    @PatchMapping("/cancel-batch/{batchId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<BatchResponse> cancel(@PathVariable UUID batchId) {
        return ResponseEntity.ok(batchService.cancelBatch(batchId));
    }

    @GetMapping("/find-bysupplier/{supplierId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN') or hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<Page<BatchResponse>> findBySupplier(
            @PathVariable UUID supplierId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(batchService.findBySupplier(supplierId, pageable));
    }

    @GetMapping("/find-my-available")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<Page<BatchResponse>> findMyAvailable(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(batchService.findMyAvailableBatches(pageable));
    }

    @GetMapping("/find-by-input/{inputId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BatchResponse>> findByInput(
            @PathVariable UUID inputId, Pageable pageable) {
        return ResponseEntity.ok(batchService.findByInput(inputId, pageable));
    }

    @GetMapping("/find-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<BatchResponse>> findByProperty(
            @PathVariable UUID propertyId, Pageable pageable) {
        return ResponseEntity.ok(batchService.findByProperty(propertyId, pageable));
    }

    @GetMapping("/find-near-expiration/{propertyId}/near-expiration")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<BatchResponse>> findNearExpiration(
            @PathVariable UUID propertyId,
            @RequestParam(defaultValue = "15") int daysAlert,
            Pageable pageable) {
        return ResponseEntity.ok(
                batchService.findNearExpiration(propertyId, daysAlert, pageable));
    }

    @GetMapping("/find-expired/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<BatchResponse>> findExpired(
            @PathVariable UUID propertyId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(batchService.findExpired(propertyId, pageable));
    }
}
