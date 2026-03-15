package tech.agrowerk.application.dto.user;

import java.util.UUID;

public record UserInfoDto(
        UUID id,
        String name,
        String email,
        String role
) {
}
