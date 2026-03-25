package tech.agrowerk.application.controller.barter;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.barter.CreateBarterOfferRequest;
import tech.agrowerk.application.dto.request.barter.UpdateBarterOfferRequest;
import tech.agrowerk.application.dto.response.barter.BarterOfferResponse;
import tech.agrowerk.business.service.barter.BarterOfferService;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.util.UUID;

@RestController
@RequestMapping("/barter-offers")
public class BarterOfferController {

    private final BarterOfferService service;

    public BarterOfferController(BarterOfferService service) {
        this.service = service;
    }

    @PostMapping("/create-offer")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BarterOfferResponse> createOffer(@Valid @RequestBody CreateBarterOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createOffer(request));
    }

    @GetMapping("/list-active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BarterOfferResponse>> listActive(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listActive(pageable));
    }

    @GetMapping("/list-by-region")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BarterOfferResponse>> listByRegion(
            @RequestParam String region, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listByRegion(region, pageable));
    }

    @GetMapping("/list-by-crop/{cropId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BarterOfferResponse>> listByCrop(
            @PathVariable UUID cropId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listByCrop(cropId, pageable));
    }

    @GetMapping("/list-by-type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BarterOfferResponse>> listByType(
            @RequestParam OfferType offerType, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listByType(offerType, pageable));
    }

    @GetMapping("/list-my-offers")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<BarterOfferResponse>> listMyOffers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.listMyOffers(pageable));
    }

    @GetMapping("/find-by-id/{barterOfferId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BarterOfferResponse> findById(@PathVariable UUID barterOfferId) {
        return ResponseEntity.ok(service.findById(barterOfferId));
    }

    @PutMapping("/update-offer/{barterOfferId}")
    @PreAuthorize("hasRole('PRODUCER')")
    public ResponseEntity<BarterOfferResponse> updateOffer(
            @PathVariable UUID barterOfferId,
            @Valid @RequestBody UpdateBarterOfferRequest request) {
        return ResponseEntity.ok(service.updateOffer(barterOfferId, request));
    }

    @PatchMapping("/cancel/{barterOfferId}")
    @PreAuthorize("hasRole('PRODUCER')")
    public ResponseEntity<Void> cancelOffer(@PathVariable UUID barterOfferId) {
        service.cancelOffer(barterOfferId);
        return ResponseEntity.noContent().build();
    }
}