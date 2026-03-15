package tech.agrowerk.application.controller.inventory;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.inventory.CreateInputCropRequest;
import tech.agrowerk.application.dto.response.inventory.InputCropResponse;
import tech.agrowerk.business.service.inventory.InputCropService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/input-crops")
public class InputCropController {

    private final InputCropService inputCropService;

    public InputCropController(InputCropService inputCropService) {
        this.inputCropService = inputCropService;
    }

    @PostMapping("/suggest-input-crop")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN') " +
            "or hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<InputCropResponse> suggestInputCrop(
            @Valid @RequestBody CreateInputCropRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inputCropService.suggestInputCrop(request));
    }

    @PatchMapping("/approve-input-crop/{inputCropId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<InputCropResponse> approveInputCrop(
            @PathVariable UUID inputCropId) {
        return ResponseEntity.ok(
                inputCropService.approve(inputCropId));
    }

    @DeleteMapping("reject-input-crop/{inputCropId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> rejectInputCrop(
            @PathVariable UUID inputCropId) {
        inputCropService.reject(inputCropId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/find-approved-by-crop/{cropId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<InputCropResponse>> findApprovedByCrop(
            @PathVariable UUID cropId) {
        return ResponseEntity.ok(
                inputCropService.findApprovedByCrop(cropId));
    }

    @GetMapping("/find-pending")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<InputCropResponse>> findPending() {
        return ResponseEntity.ok(inputCropService.findPending());
    }

    @GetMapping("/find-my-pending")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN')")
    public ResponseEntity<List<InputCropResponse>> findMyPending() {
        return ResponseEntity.ok(inputCropService.findMyPending());
    }
}
