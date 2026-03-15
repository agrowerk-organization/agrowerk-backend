package tech.agrowerk.application.dto.response.core;


import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String telephone,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
}
