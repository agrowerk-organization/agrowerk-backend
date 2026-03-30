package tech.agrowerk.business.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.support.SupportTicket;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketStatus;
import tech.agrowerk.infrastructure.repository.support.SupportTicketRepository;

import java.util.UUID;

@Component
@Slf4j
public class SupportAccessValidator {

    private final SupportTicketRepository supportTicketRepository;
    private final AuthUtil authUtil;

    public SupportAccessValidator(SupportTicketRepository supportTicketRepository, AuthUtil authUtil) {
        this.supportTicketRepository = supportTicketRepository;
        this.authUtil = authUtil;
    }

    public SupportTicket validateTicketAccess(UUID ticketId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        SupportTicket supportTicket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        boolean isAdmin = auth.role().equals("SYSTEM_ADMIN");
        boolean isOwner = supportTicket.getUser().getId().equals(auth.id());

        if (!isAdmin && !isOwner) {
            log.warn("Access denied: user {} tried to access ticket {}", auth.id(), ticketId);
            throw new AccessDeniedException("You don´t have access to this ticket");
        }

        return supportTicket;
    }

    public SupportTicket validateAdminOnly(UUID ticketId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        SupportTicket supportTicket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        if (!auth.role().equals("SYSTEM_ADMIN")) {
            log.warn("Access denied: user {} tried admin action on ticket {}", auth.id(), ticketId);
            throw new AccessDeniedException("Only admins can perform this action");
        }

        return supportTicket;
    }

    public void validateTicketOpen(SupportTicket supportTicket) {
        if (supportTicket.getSupportTicketStatus() == SupportTicketStatus.RESOLVED
            || supportTicket.getSupportTicketStatus() == SupportTicketStatus.CANCELLED) {
            throw new IllegalStateException("Cannot perform this action on a closed ticket");
        }
    }
}
