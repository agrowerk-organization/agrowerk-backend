package tech.agrowerk.application.controller.farming;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.farming.CreatePrescriptionRequest;
import tech.agrowerk.application.dto.response.farming.PrescriptionResponse;
import tech.agrowerk.business.service.farming.AgronomicPrescriptionService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agronomic-prescriptions")
public class AgronomicPrescriptionController {

    private final AgronomicPrescriptionService prescriptionService;

    public AgronomicPrescriptionController(AgronomicPrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping(value = "/create-prescription",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PrescriptionResponse> create(
            @RequestPart("data") @Valid CreatePrescriptionRequest request,
            @RequestPart("document") MultipartFile document) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.createPrescription(request, document));
    }

    @PatchMapping("deactivate-prescription/{prescriptionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PrescriptionResponse> deactivate(
            @PathVariable UUID prescriptionId) {
        return ResponseEntity.ok(
                prescriptionService.deactivatePrescription(prescriptionId));
    }

    @GetMapping("/find-by-planting/{plantingId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<PrescriptionResponse>> findByPlanting(
            @PathVariable UUID plantingId) {
        return ResponseEntity.ok(
                prescriptionService.findByPlanting(plantingId));
    }

    @GetMapping("/find-near-expiration/{propertyId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<PrescriptionResponse>> findNearExpiration(
            @PathVariable UUID propertyId) {
        return ResponseEntity.ok(
                prescriptionService.findNearExpiration(propertyId));
    }
}