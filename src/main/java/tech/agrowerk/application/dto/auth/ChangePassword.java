package tech.agrowerk.application.dto.auth;

public record ChangePassword(
        String newPassword,
        String confirmPassword
) {
}
