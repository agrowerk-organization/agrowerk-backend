package tech.agrowerk.application.dto.auth;

public record LoginRequest(
        String email,
        String password
) {
}
