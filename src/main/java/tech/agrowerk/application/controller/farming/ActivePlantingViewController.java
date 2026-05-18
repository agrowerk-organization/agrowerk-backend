package tech.agrowerk.application.controller.farming;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.views.ActivePlantingResponse;
import tech.agrowerk.business.service.farming.ActivePlantingViewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/active-planting")
public class ActivePlantingViewController {

    private final ActivePlantingViewService activePlantingViewService;

    public ActivePlantingViewController(ActivePlantingViewService activePlantingViewService) {
        this.activePlantingViewService = activePlantingViewService;
    }

    @GetMapping("/get-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<ActivePlantingResponse> getByPlanting(@PathVariable UUID plantingId) {
        return activePlantingViewService.findActivePlantingByPlantingId(plantingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/get-by-property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<ActivePlantingResponse>> getByProperty(
            @PathVariable UUID propertyId) {

        List<ActivePlantingResponse> response = activePlantingViewService.findByProperty(propertyId);

        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
}
