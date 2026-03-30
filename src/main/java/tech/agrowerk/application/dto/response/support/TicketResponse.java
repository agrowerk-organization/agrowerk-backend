package tech.agrowerk.application.dto.response.support;

import tech.agrowerk.infrastructure.model.support.enums.SupportTicketPriority;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketStatus;
import tech.agrowerk.infrastructure.model.support.enums.TicketCategory;

import java.util.UUID;
import java.time.Instant;

public record TicketResponse(
        UUID id,
        String subject,
        String description,
        TicketCategory ticketCategory,
        String ticketCategoryDisplay,
        SupportTicketPriority supportTicketPriority,
        String ticketPriorityDisplay,
        SupportTicketStatus supportTicketStatus,
        String userName,
        String assignedUserName,
        Instant createdAt,
        Instant updatedAt
) {
}
