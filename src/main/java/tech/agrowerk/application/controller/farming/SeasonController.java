package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateSeasonRequest;
import tech.agrowerk.application.dto.response.farming.SeasonResponse;
import tech.agrowerk.business.service.farming.SeasonService;

import java.util.UUID;

@RestController
@RequestMapping("/seasons")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @PostMapping("/create-season")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SeasonResponse> create(@Valid @RequestBody CreateSeasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seasonService.createSeason(request));
    }

    @PatchMapping("activate-season/{seasonId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SeasonResponse> activate(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(seasonService.activateSeason(seasonId));
    }

    @PatchMapping("finish-season/{seasonId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<SeasonResponse> finish(@PathVariable UUID seasonId) {
        return ResponseEntity.ok(seasonService.finishSeason(seasonId));
    }

    @GetMapping("/my-season/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<SeasonResponse>> mySeasons(
            @PathVariable UUID propertyId,
            @PageableDefault(size = 10)Pageable pageable
            ) {
        return ResponseEntity.ok(seasonService.findMySeasons(propertyId, pageable));
    }
}