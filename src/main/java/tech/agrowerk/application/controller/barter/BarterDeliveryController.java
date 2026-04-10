package tech.agrowerk.application.controller.barter;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.barter.RegisterPartialDeliveryRequest;
import tech.agrowerk.application.dto.response.barter.CropCommitmentResponse;
import tech.agrowerk.application.dto.response.barter.PartialDeliveryResponse;
import tech.agrowerk.business.service.barter.BarterDeliveryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/barter")
public class BarterDeliveryController {

    private final BarterDeliveryService service;

    public BarterDeliveryController(BarterDeliveryService service) {
        this.service = service;
    }

    @GetMapping("/find-my-commitments")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<CropCommitmentResponse>> listMyCommitments() {
        return ResponseEntity.ok(service.listMyCommitments());
    }

    @GetMapping("/list-by-transactions/{transactionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<CropCommitmentResponse>> listByTransaction(
            @PathVariable UUID transactionId) {
        return ResponseEntity.ok(service.listCommitmentsByTransaction(transactionId));
    }

    @PostMapping("/register-delivery")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<PartialDeliveryResponse> registerDelivery(
            @Valid @RequestBody RegisterPartialDeliveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registerDelivery(request));
    }

    @GetMapping("/list-deliveries/{commitmentId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<List<PartialDeliveryResponse>> listDeliveries(
            @PathVariable UUID commitmentId) {
        return ResponseEntity.ok(service.listDeliveriesByCommitment(commitmentId));
    }
}