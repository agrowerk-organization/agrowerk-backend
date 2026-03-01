package tech.agrowerk.application.dto.response;


import java.time.Instant;

public record UserResponse(
        java.util.UUID id,
        String name,
        String email,
        String telephone,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
}
