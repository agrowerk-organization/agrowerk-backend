package tech.agrowerk.application.controller.inventory;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.inventory.StockAdjustmentRequest;
import tech.agrowerk.application.dto.request.inventory.StockTransferRequest;
import tech.agrowerk.application.dto.response.inventory.StockResponse;
import tech.agrowerk.business.service.inventory.StockService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/find-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<StockResponse>> findByProperty(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(
                stockService.findByProperty(propertyId));
    }

    @GetMapping("/find-alerts/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<StockResponse>> findAlerts(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(
                stockService.findAlerts(propertyId));
    }

    @PostMapping("/adjust-stock")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<StockResponse> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(
                stockService.adjustStock(request));
    }

    @PostMapping("/transfer-stock")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> transferStock(
            @Valid @RequestBody StockTransferRequest request) {
        stockService.transferStock(request);
        return ResponseEntity.noContent().build();
    }
}