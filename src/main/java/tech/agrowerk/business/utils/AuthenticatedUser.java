package tech.agrowerk.business.utils;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email,
        String role
) {
}
