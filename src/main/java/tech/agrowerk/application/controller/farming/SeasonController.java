package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.create.CreateSeasonRequest;
import tech.agrowerk.application.dto.response.SeasonResponse;
import tech.agrowerk.business.service.farming.SeasonService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/seasons")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SeasonResponse> create(@Valid @RequestBody CreateSeasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seasonService.createSeason(request));
    }

    @PatchMapping("/{seasonId}/activate")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SeasonResponse> activate(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(seasonService.activateSeason(seasonId));
    }

    @PatchMapping("/{seasonId}/finish")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SeasonResponse> finish(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(seasonService.finishSeason(seasonId));
    }

    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<SeasonResponse>> mySeasons(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(seasonService.findMySeasons(propertyId));
    }
}