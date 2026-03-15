package tech.agrowerk.application.controller.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.response.inventory.StockMovementResponse;
import tech.agrowerk.business.service.inventory.StockMovementViewService;

import java.util.UUID;

@RestController
@RequestMapping("/stock-movement-view")
public class StockMovementViewController {

    private final StockMovementViewService movementService;

    public StockMovementViewController(StockMovementViewService movementService) {
        this.movementService = movementService;
    }

    @GetMapping("/get-movements/{propertyId}")
    public ResponseEntity<Page<StockMovementResponse>> getMovements(
            @PathVariable UUID propertyId,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {

        if (type != null) {
            return ResponseEntity.ok(movementService.findByPropertyAndType(propertyId, type, pageable));
        }

        return ResponseEntity.ok(movementService.findByProperty(propertyId, pageable));
    }
}