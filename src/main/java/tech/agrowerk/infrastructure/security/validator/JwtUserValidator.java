package tech.agrowerk.infrastructure.security.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.security.services.TokenBlacklistService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtUserValidator {

    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public User validate(UUID userId, Integer tokenVersion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.isDeleted()) {
            throw new DisabledException("User account has been deleted");
        }

        if (!user.isActive()) {
            throw new DisabledException("User account is inactive");
        }

        if (user.isAccountLocked()) {
            throw new LockedException("User account is locked until " + user.getLockedUntil());
        }

        if (!user.isEmailVerified()) {
            throw new DisabledException("Email not verified");
        }

        if (tokenVersion == null || !tokenVersion.equals(user.getTokenVersion())) {
            log.warn("Token version mismatch for user {}. Expected: {}, Got: {}",
                    userId, user.getTokenVersion(), tokenVersion);
            throw new AccessDeniedException("Token has been revoked");
        }

        return user;
    }

    public void validateToken(Jwt jwt) {
        String jti = jwt.getClaimAsString("jti");

        if (tokenBlacklistService.isBlacklisted(jti)) {
            throw new AccessDeniedException("Token has been revoked");
        }

        String userIdString = jwt.getClaimAsString("userId");
        Number tvClaim = jwt.getClaim("tv");
        Integer tokenVersion = (tvClaim != null) ? tvClaim.intValue() : null;
        Instant issuedAt = jwt.getIssuedAt();

        if (userIdString == null) {
            throw new AccessDeniedException("Invalid token: missing userId");
        }

        UUID userId = UUID.fromString(userIdString);
        User user = validate(userId, tokenVersion);

        if (issuedAt != null && tokenBlacklistService.isTokenIssuedBeforeUserBlacklist(userId, issuedAt)) {
            throw new AccessDeniedException("Token was issued before user blacklist");
        }
    }
}