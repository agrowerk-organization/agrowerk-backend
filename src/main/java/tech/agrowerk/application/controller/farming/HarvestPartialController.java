package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateHarvestPartialRequest;
import tech.agrowerk.application.dto.request.farming.UpdateHarvestPartialRequest;
import tech.agrowerk.application.dto.response.farming.HarvestPartialResponse;
import tech.agrowerk.business.service.farming.HarvestPartialService;

import java.util.UUID;

@RestController
@RequestMapping("/harvest-partials")
public class HarvestPartialController {

    private final HarvestPartialService harvestPartialService;

    public HarvestPartialController(HarvestPartialService harvestPartialService) {
        this.harvestPartialService = harvestPartialService;
    }

    @PostMapping("/create-partial/{harvestId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestPartialResponse> addPartial(
            @PathVariable UUID harvestId,
            @Valid @RequestBody CreateHarvestPartialRequest request
            ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestPartialService.addPartial(harvestId, request));
    }

    @GetMapping("find-by-harvest/{harvestId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<HarvestPartialResponse>> findByPartial(
            @PathVariable UUID harvestId,
            @PageableDefault(size = 10)Pageable pageable
            ) {
        return ResponseEntity.ok(
                harvestPartialService.findByHarvest(harvestId, pageable)
        );
    }

    @PutMapping("/{partialId}/update")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<HarvestPartialResponse> update(
            @PathVariable UUID partialId,
            @Valid @RequestBody UpdateHarvestPartialRequest request) {
        return ResponseEntity.ok(
                harvestPartialService.updatePartial(partialId, request));
    }
}
