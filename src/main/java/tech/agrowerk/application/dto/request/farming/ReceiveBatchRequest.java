package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReceiveBatchRequest(
        @NotNull
        UUID propertyId,

        @NotNull
        UUID warehouseId
) {
}
