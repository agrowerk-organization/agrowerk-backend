package tech.agrowerk.application.dto.request.support;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TicketMessageRequest(
        @NotBlank
        String message,

        List<String> attachments
) {
}
