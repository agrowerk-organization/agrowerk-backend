package tech.agrowerk.application.dto.response.support;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TicketMessageResponse(
        UUID id,
        UUID ticketId,
        String userName,
        String message,
        boolean isInternal,
        List<String> attachments,
        Instant createdAt
) {
}
