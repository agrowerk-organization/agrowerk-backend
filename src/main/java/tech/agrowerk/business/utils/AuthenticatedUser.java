package tech.agrowerk.business.utils;

import java.util.Set;

public record AuthenticatedUser(
        Long id,
        String email,
        String role
) {
}
