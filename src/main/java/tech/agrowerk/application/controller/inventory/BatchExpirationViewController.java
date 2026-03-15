package tech.agrowerk.application.controller.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.response.inventory.BatchExpirationResponse;
import tech.agrowerk.business.service.inventory.BatchExpirationViewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/batch-expiration-views")
public class BatchExpirationViewController {

    private final BatchExpirationViewService batchService;

    public BatchExpirationViewController(BatchExpirationViewService batchService) {
        this.batchService = batchService;
    }

    @GetMapping("/get-expiring/{propertyId}")
    public ResponseEntity<List<BatchExpirationResponse>> getExpiringBatches(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(batchService.findByProperty(propertyId));
    }

    @GetMapping("/get-critical/{propertyId}")
    public ResponseEntity<List<BatchExpirationResponse>> getCriticalBatches(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(batchService.findCritical(propertyId));
    }
}