package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreatePlantingInputRequest;
import tech.agrowerk.application.dto.response.farming.PlantingInputResponse;
import tech.agrowerk.business.service.farming.PlantingInputService;

import java.util.UUID;

@RestController
@RequestMapping("/planning-inputs")
public class PlantingInputController {

    private final PlantingInputService plantingInputService;

    public PlantingInputController(PlantingInputService plantingInputService) {
        this.plantingInputService = plantingInputService;
    }

    @PostMapping("/create-planting-input")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PlantingInputResponse> register(
            @Valid @RequestBody CreatePlantingInputRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plantingInputService.createInput(request));
    }

    @GetMapping("/find-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<PlantingInputResponse>> findByPlanting(
            @PathVariable UUID plantingId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(plantingInputService.findByPlanting(plantingId, pageable));
    }

    @GetMapping("/find-by-input/{inputId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<PlantingInputResponse>> findByInput(
            @PathVariable UUID inputId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(plantingInputService.findByInput(inputId, pageable));
    }
}
