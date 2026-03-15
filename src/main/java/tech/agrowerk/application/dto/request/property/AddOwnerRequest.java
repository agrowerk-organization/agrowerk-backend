package tech.agrowerk.application.dto.request.property;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddOwnerRequest(
        @NotNull UUID userId,
        boolean canEdit
) {
}
