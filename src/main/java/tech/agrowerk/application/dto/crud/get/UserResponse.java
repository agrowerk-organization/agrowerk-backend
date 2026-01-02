package tech.agrowerk.application.dto.crud.get;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String telephone,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
