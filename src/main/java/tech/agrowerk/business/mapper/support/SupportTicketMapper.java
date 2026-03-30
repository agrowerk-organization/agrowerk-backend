package tech.agrowerk.business.mapper.support;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.support.TicketMessageResponse;
import tech.agrowerk.application.dto.response.support.TicketResponse;
import tech.agrowerk.infrastructure.model.support.SupportMessage;
import tech.agrowerk.infrastructure.model.support.SupportTicket;

@Component
public class SupportTicketMapper {

    public TicketResponse toResponse(SupportTicket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getTicketCategory(),
                ticket.getTicketCategory().getDisplayName(),
                ticket.getTicketPriority(),
                ticket.getTicketPriority().getDisplayName(),
                ticket.getSupportTicketStatus(),
                ticket.getUser().getName(),
                ticket.getAssignedUser() != null ? ticket.getAssignedUser().getName() : null,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketMessageResponse toMessageResponse(SupportMessage message) {
        return new TicketMessageResponse(
                message.getId(),
                message.getTicket().getId(),
                message.getUser().getName(),
                message.getMessage(),
                message.isInternal(),
                message.getAttachments(),
                message.getCreatedAt()
        );
    }
}
