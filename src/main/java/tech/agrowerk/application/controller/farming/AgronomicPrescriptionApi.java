/*package tech.agrowerk.application.controller.farming;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.farming.CreatePrescriptionRequest;
import tech.agrowerk.application.dto.response.farming.PrescriptionResponse;

import java.util.List;
import java.util.UUID;

@Tag(name = "Agronomic Prescriptions", description = "Management of technical documents and prescriptions for crop treatments")
public interface AgronomicPrescriptionApi {

    @Operation(summary = "Upload prescription", description = "Registers a new prescription with an attached PDF/Image document.")
    ResponseEntity<PrescriptionResponse> create(
            @RequestPart("data") CreatePrescriptionRequest request,
            @RequestPart("document") MultipartFile document);

    @Operation(summary = "Deactivate prescription", description = "Marks a prescription as inactive (soft delete).")
    ResponseEntity<PrescriptionResponse> deactivate(@PathVariable UUID prescriptionId);

    @Operation(summary = "Find near expiration", description = "Lists prescriptions close to their expiration date for a property.")
    ResponseEntity<List<PrescriptionResponse>> findNearExpiration(@PathVariable UUID propertyId);
} */