package tech.agrowerk.application.controller.farming;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.views.HarvestDashboardResponse;
import tech.agrowerk.business.service.farming.HarvestDashboardViewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/harvest-dashboard")
public class HarvestDashboardViewController {

    private final HarvestDashboardViewService harvestDashboardViewService;

    public HarvestDashboardViewController(HarvestDashboardViewService harvestDashboardViewService) {
        this.harvestDashboardViewService = harvestDashboardViewService;
    }

    @GetMapping("/get-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<HarvestDashboardResponse>> getByProperty(
            @PathVariable UUID propertyId) {

        List<HarvestDashboardResponse> response = harvestDashboardViewService.findByProperty(propertyId);

        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestDashboardResponse> getByPlanting(
            @PathVariable UUID plantingId) {

        return harvestDashboardViewService.findByPlanting(plantingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
