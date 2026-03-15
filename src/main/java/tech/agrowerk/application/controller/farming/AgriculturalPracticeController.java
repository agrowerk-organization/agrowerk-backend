package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.farming.CreateAgriculturalPracticeRequest;
import tech.agrowerk.application.dto.response.farming.AgriculturalPracticeResponse;
import tech.agrowerk.business.service.farming.AgriculturalPracticeService;
import tech.agrowerk.infrastructure.model.farming.enums.PractipeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/agricultural-practices")
@RequiredArgsConstructor
public class AgriculturalPracticeController {

    private final AgriculturalPracticeService practiceService;

    @PostMapping("/create-agricultural-practice")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<AgriculturalPracticeResponse> createPractice(
            @Valid @RequestBody CreateAgriculturalPracticeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(practiceService.createPractice(request));
    }

    @GetMapping("/find-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<AgriculturalPracticeResponse>> findByPlanting(
            @PathVariable UUID plantingId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(practiceService.findByPlanting(plantingId, pageable));
    }

    @GetMapping("/find-by-planting-type/{plantingId}/type/{type}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<AgriculturalPracticeResponse>> findByType(
            @PathVariable UUID plantingId,
            @PathVariable PractipeType type,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                practiceService.findByPlantingAndType(plantingId, type, pageable));
    }

    @GetMapping("/get-total-cost-by-planting/{plantingId}/cost")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BigDecimal> getTotalCost(@PathVariable UUID plantingId) {
        return ResponseEntity.ok(practiceService.getTotalCostByPlanting(plantingId));
    }

    @GetMapping("/property/{propertyId}/cost")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BigDecimal> getCostByPeriod(
            @PathVariable UUID propertyId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return ResponseEntity.ok(
                practiceService.getTotalCostByPropertyAndPeriod(propertyId, start, end));
    }
}