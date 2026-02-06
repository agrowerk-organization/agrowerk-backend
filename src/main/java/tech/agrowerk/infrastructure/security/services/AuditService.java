package tech.agrowerk.infrastructure.security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.model.audit.AuditLog;
import tech.agrowerk.infrastructure.model.audit.enums.SecurityEvent;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.audit.AuditLogRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logSecurityEvent(SecurityEvent event, String ip, String userAgent,
                                 UUID userId, String details) {
        AuditLog audit = AuditLog.builder()
                .eventType(event)
                .ipAddress(ip)
                .userAgent(userAgent)
                .userId(userId)
                .details(details)
                .timestamp(Instant.now())
                .build();

        auditLogRepository.save(audit);

        if (event.isCritical()) {
            log.error("SECURITY EVENT: {} - User: {} - IP: {} - Details: {}",
                    event, userId, ip, details);
        }
    }

    public void logLogin(User user, String ip, boolean success) {
        logSecurityEvent(
                success ? SecurityEvent.LOGIN_SUCCESS : SecurityEvent.LOGIN_FAILED,
                ip, null, user.getId(),
                success ? "Login successfull" : "Login attempted failed"
        );
    }

    public void logPasswordChange(User user, String ip) {
        logSecurityEvent(SecurityEvent.PASSWORD_CHANGED, ip, null,
                user.getId(), "Changed password");
    }

    public void logAccountLocked(User user, String ip) {
        logSecurityEvent(SecurityEvent.ACCOUNT_LOCKED, ip, null,
                user.getId(), "Account blocked due to multiple attempts.");
    }

    public void logSuspiciousActivity(UUID userId, String ip, String reason) {
        logSecurityEvent(SecurityEvent.SUSPICIOUS_ACTIVITY, ip, null,
                userId, reason);
    }
}