package tech.agrowerk.application.controller.barter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.barter.AcceptTransactionRequest;
import tech.agrowerk.application.dto.request.barter.ProposeTransactionRequest;
import tech.agrowerk.application.dto.request.barter.SignContractRequest;
import tech.agrowerk.application.dto.response.barter.BarterContractResponse;
import tech.agrowerk.application.dto.response.barter.BarterTransactionResponse;
import tech.agrowerk.business.service.barter.BarterTransactionService;

import java.util.UUID;

@RestController
@RequestMapping("/barter-transactions")
public class BarterTransactionController {

    private final BarterTransactionService barterTransactionService;

    public BarterTransactionController(BarterTransactionService barterTransactionService) {
        this.barterTransactionService = barterTransactionService;
    }

    @PostMapping("/propose-transaction")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BarterTransactionResponse> proposeTransaction(@Valid @RequestBody ProposeTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(barterTransactionService.proposeTransaction(request));
    }

    @GetMapping("/list-my-transactions")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<BarterTransactionResponse>> listMine(@PageableDefault(size = 10)Pageable pageable) {
        return ResponseEntity.ok(barterTransactionService.listMyTransactions(pageable));
    }

    @PatchMapping("/accept-transaction/{barterTransactionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BarterTransactionResponse> acceptTransaction(
            @PathVariable UUID barterTransactionId,
            @Valid @RequestBody AcceptTransactionRequest request) {
        return ResponseEntity.ok(
                barterTransactionService.acceptTransaction(barterTransactionId, request));
    }

    @GetMapping("/find-by-id/{barterTransactionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BarterTransactionResponse> findById(@PathVariable UUID barterTransactionId) {
        return ResponseEntity.ok(barterTransactionService.findById(barterTransactionId));
    }

    @PatchMapping("/decline-transaction/{barterTransactionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> declineTransaction(@PathVariable UUID barterTransactionId) {
        barterTransactionService.declineTransaction(barterTransactionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cancel-transaction/{barterTransactionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Void> cancelTransaction(@PathVariable UUID barterTransactionId) {
        barterTransactionService.cancelTransaction(barterTransactionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contract-transaction/{barterTransactionId}")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BarterContractResponse> findContract(@PathVariable UUID barterTransactionId) {
        return ResponseEntity.ok(barterTransactionService.findContract(barterTransactionId));
    }

    @PostMapping("/sign-contract")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<BarterContractResponse> signContract(
            @Valid @RequestBody SignContractRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(barterTransactionService.signContract(request, httpRequest));
    }
}
