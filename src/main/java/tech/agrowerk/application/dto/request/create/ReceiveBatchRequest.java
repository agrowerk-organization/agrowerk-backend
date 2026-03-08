package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReceiveBatchRequest(
        @NotNull
        UUID propertyId
) {
}
