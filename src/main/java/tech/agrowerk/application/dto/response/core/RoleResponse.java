package tech.agrowerk.application.dto.response.core;

import java.util.UUID;

public record RoleResponse(
    UUID roleId,
    String name
) {
}
