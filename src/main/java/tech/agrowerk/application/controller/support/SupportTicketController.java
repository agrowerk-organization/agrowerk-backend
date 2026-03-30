package tech.agrowerk.application.controller.support;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.cache.CachedPage;
import tech.agrowerk.application.dto.request.support.TicketRequest;
import tech.agrowerk.application.dto.response.support.TicketResponse;
import tech.agrowerk.business.service.support.SupportTicketService;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketStatus;

import java.util.UUID;

@RestController
@RequestMapping("/support-tickets")
public class SupportTicketController {
    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping("/create-ticket")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN')")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportTicketService.createTicket(request));
    }

    @GetMapping("/get-mine")
    @PreAuthorize("hasAnyAuthority('PRODUCER', 'SUPPLIER_ADMIN')")
    public ResponseEntity<CachedPage<TicketResponse>> findMine(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supportTicketService.findMine(pageable));
    }

    @GetMapping("/find-all")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<CachedPage<TicketResponse>> findAll(
            @RequestParam(required = false) SupportTicketStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(supportTicketService.findAll(status, pageable));
    }

    @GetMapping("/find-by-id/{ticketId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> findById(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportTicketService.findById(ticketId));
    }

    @PatchMapping("/update-status/{ticketId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable UUID ticketId,
            @RequestParam SupportTicketStatus status) {
        return ResponseEntity.ok(supportTicketService.updateTicketStatus(ticketId, status));
    }

    @PatchMapping("assign/{ticketId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID ticketId,
            @RequestParam UUID adminUserId) {
        return ResponseEntity.ok(supportTicketService.assignTicket(ticketId, adminUserId));
    }
}
