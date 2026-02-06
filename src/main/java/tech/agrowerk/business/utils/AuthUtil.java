package tech.agrowerk.business.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import tech.agrowerk.infrastructure.exception.local.IllegalStateException;

import java.util.UUID;

@Component
public class AuthUtil {

    public AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            throw new IllegalStateException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new IllegalStateException("Principal is not an instance of Jwt");
        }

        UUID userId = jwt.getClaim("userId");
        if (userId == null) {
            throw new IllegalArgumentException("userId not found in the token");
        }

        String email = jwt.getClaimAsString("email");
        String role = jwt.getClaimAsString("role");

        if (role == null) {
            throw new IllegalArgumentException("Role not found in the token");
        }

        return new AuthenticatedUser(userId, email, role);
    }
}
