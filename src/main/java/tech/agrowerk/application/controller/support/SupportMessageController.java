package tech.agrowerk.application.controller.support;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.support.TicketMessageRequest;
import tech.agrowerk.application.dto.response.support.TicketMessageResponse;
import tech.agrowerk.business.service.support.SupportMessageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/support-messages")
public class SupportMessageController {
    private final SupportMessageService supportMessageService;

    public SupportMessageController(SupportMessageService supportMessageService) {
        this.supportMessageService = supportMessageService;
    }

    @GetMapping("/find-messages/{ticketId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TicketMessageResponse>> findMessages(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportMessageService.findMessages(ticketId));
    }

    @PostMapping("/add-message/{ticketId}")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<TicketMessageResponse> addMessage(@PathVariable UUID ticketId,
                                                            @Valid @RequestBody TicketMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportMessageService.addMessage(ticketId, request));
    }
}

