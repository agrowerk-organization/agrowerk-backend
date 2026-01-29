package tech.agrowerk.infrastructure.model.audit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecurityEvent {
    LOGIN_SUCCESS(false),
    LOGIN_FAILED(false),
    LOGOUT(false),
    PASSWORD_CHANGED(true),
    PASSWORD_RESET_REQUESTED(false),
    PASSWORD_RESET_COMPLETED(true),
    ACCOUNT_LOCKED(true),
    ACCOUNT_UNLOCKED(true),
    MFA_ENABLED(true),
    MFA_DISABLED(true),
    EMAIL_CHANGED(true),
    OAUTH_LINKED(false),
    TOKEN_REFRESHED(false),
    TOKEN_REVOKED(true),
    SUSPICIOUS_ACTIVITY(true),
    RATE_LIMIT_EXCEEDED(false),
    UNAUTHORIZED_ACCESS_ATTEMPT(true),
    DATA_EXPORTED(true),
    DATA_ANONYMIZED(true),
    ACCOUNT_DELETED(true);

    private final boolean critical;
}