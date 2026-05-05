package tech.agrowerk.application.controller.farming;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.views.SeasonDashboardResponse;
import tech.agrowerk.business.service.farming.SeasonDashboardService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/season-dashboard")
public class SeasonDashboardController {

    private final SeasonDashboardService seasonDashboardService;

    public SeasonDashboardController(SeasonDashboardService seasonDashboardService) {
        this.seasonDashboardService = seasonDashboardService;
    }

    @GetMapping("/get-dashboard/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<SeasonDashboardResponse>> getDashboard(
            @PathVariable UUID propertyId) {

        List<SeasonDashboardResponse> response = seasonDashboardService.getDashboard(propertyId);

        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-season/{seasonId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<SeasonDashboardResponse>> getDashboardBySeason(
            @PathVariable UUID seasonId) {

        List<SeasonDashboardResponse> response = seasonDashboardService.getDashboardBySeason(seasonId);

        if (response.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }
}