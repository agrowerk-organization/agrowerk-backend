package tech.agrowerk.application.dto.response.core;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        String telephone,
        String cpf,
        AddressResponse addressResponse,
        boolean emailVerified,
        boolean phoneVerified,
        boolean mfaEnabled,
        Instant lastLogin,
        Instant lastPasswordChange,
        boolean requirePasswordChange,
        boolean termsAccepted,
        boolean privacyPolicyAccepted,
        boolean marketingConsent,
        Instant createdAt,
        String avatarUrl
) {
}
