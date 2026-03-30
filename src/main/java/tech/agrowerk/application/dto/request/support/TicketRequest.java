package tech.agrowerk.application.dto.request.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.agrowerk.infrastructure.model.support.enums.SupportTicketPriority;
import tech.agrowerk.infrastructure.model.support.enums.TicketCategory;

public record TicketRequest(
        @NotBlank
        String subject,

        @NotBlank
        String description,

        @NotNull
        TicketCategory ticketCategory,

        @NotNull
        SupportTicketPriority supportTicketPriority
) {
}
