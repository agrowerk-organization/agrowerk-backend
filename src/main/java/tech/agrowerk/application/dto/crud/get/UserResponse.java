package tech.agrowerk.application.dto.crud.get;


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
