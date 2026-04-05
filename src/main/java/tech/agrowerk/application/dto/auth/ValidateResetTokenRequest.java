package tech.agrowerk.application.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ValidateResetTokenRequest(
        @NotBlank
        String token
) {
}
