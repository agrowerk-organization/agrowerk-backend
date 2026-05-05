package tech.agrowerk.application.controller.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.views.StockPositionResponse;
import tech.agrowerk.business.service.inventory.StockPositionViewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/stock-position-views")
public class StockPositionViewController {

    private final StockPositionViewService positionService;

    public StockPositionViewController(StockPositionViewService positionService) {
        this.positionService = positionService;
    }

    @GetMapping("/get-positions/{propertyId}")
    public ResponseEntity<List<StockPositionResponse>> getPositions(
            @PathVariable UUID propertyId,
            @RequestParam(required = false) String alert) {

        if (alert != null) {
            return ResponseEntity.ok(positionService.findByPropertyAndAlert(propertyId, alert));
        }

        return ResponseEntity.ok(positionService.findByProperty(propertyId));
    }
}