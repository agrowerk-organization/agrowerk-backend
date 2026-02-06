package tech.agrowerk.business.service.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResult;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.mapper.UserMapper;
import tech.agrowerk.infrastructure.exception.local.AuthenticationException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.audit.enums.SecurityEvent;
import tech.agrowerk.infrastructure.security.services.AuditService;
import tech.agrowerk.infrastructure.security.services.CookieService;
import tech.agrowerk.infrastructure.security.services.JwtService;
import tech.agrowerk.infrastructure.exception.local.BadCredentialsException;
import tech.agrowerk.infrastructure.exception.local.InvalidTokenException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.security.services.TokenBlacklistService;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuditService auditService;
    private final UserMapper userMapper;

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final String FAKE_HASH = "$2a$12$fakehashtopreventtimingattack";


    @Transactional
    public LoginResult login(LoginRequest loginRequest, HttpServletRequest request) {
        String email = loginRequest.email();
        String password = loginRequest.password();

        User user = userRepository.findByEmail(email).orElse(null);

        boolean validPassword = false;
        if (user != null) {
            validPassword = passwordEncoder.matches(password, user.getPassword());
        } else {
            passwordEncoder.matches(password, FAKE_HASH);
        }

        if (user == null || !validPassword) {
            if (user != null) {
                handleFailedLogin(user, request);
            }

            auditService.logSecurityEvent(
                    SecurityEvent.LOGIN_FAILED,
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    user != null ? user.getId() : null,
                    "Invalid credentials for email: " + email
            );

            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.isAccountLocked()) {
            if (isUnlockTimeExpired(user)) {
                unlockAccount(user);
            } else {
                auditService.logAccountLocked(user, getClientIp(request));
                throw new LockedException("Account is locked. Try again later.");
            }
        }

        validateUserCanLogin(user);

        resetFailedAttempts(user);
        updateLastLogin(user, request);

        String accessToken = jwtService.generateTokenFromUser(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        user.setRefreshToken(refreshToken, passwordEncoder, 7);
        user.setRefreshTokenFamilyId(UUID.randomUUID().toString());
        userRepository.save(user);

        auditService.logLogin(user, getClientIp(request), true);

        return createLoginResult(user, accessToken, refreshToken);
    }

    @Transactional
    public LoginResult refreshToken(String refreshTokenValue, HttpServletRequest request) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidTokenException("Refresh token is required");
        }

        try {
            Jwt jwt = jwtService.decodeAndValidateToken(refreshTokenValue);

            String tokenType = jwt.getClaim("type");
            if (!"refresh".equals(tokenType)) {
                throw new InvalidTokenException("Invalid token type");
            }

            UUID userId = jwt.getClaim("userId");
            String username = jwt.getSubject();
            Integer tokenVersion = jwt.getClaim("tv");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            if (!user.isRefreshTokenValid(refreshTokenValue, passwordEncoder)) {
                log.warn("Invalid refresh token for user: {} - Possible token theft", user.getEmail());

                jwtService.invalidateAllUserTokens(userId);

                auditService.logSuspiciousActivity(
                        userId,
                        getClientIp(request),
                        "Invalid refresh token - possible token theft"
                );

                throw new InvalidTokenException("Invalid refresh token");
            }

            if (!tokenVersion.equals(user.getTokenVersion())) {
                throw new InvalidTokenException("Token has been revoked");
            }

            validateUserCanLogin(user);

            String newAccessToken = jwtService.generateTokenFromUser(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            user.setRefreshToken(newRefreshToken, passwordEncoder, 7);
            userRepository.save(user);

            auditService.logSecurityEvent(
                    SecurityEvent.TOKEN_REFRESHED,
                    getClientIp(request),
                    request.getHeader("User-Agent"),
                    userId,
                    "Token refreshed successfully"
            );

            return createLoginResult(user, newAccessToken, newRefreshToken);

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired refresh token");
        }
    }


    @Transactional
    public void logout(String accessToken, String refreshToken, HttpServletRequest request) {
        try {
            if (accessToken != null && !accessToken.isBlank()) {
                jwtService.invalidateToken(accessToken);
            }

            if (refreshToken != null && !refreshToken.isBlank()) {
                try {
                    Jwt jwt = jwtService.decodeAndValidateToken(refreshToken);
                    UUID userId = jwt.getClaim("userId");

                    User user = userRepository.findById(userId).orElse(null);
                    if (user != null) {
                        user.invalidateRefreshToken();
                        userRepository.save(user);

                        auditService.logSecurityEvent(
                                SecurityEvent.LOGOUT,
                                getClientIp(request),
                                request.getHeader("User-Agent"),
                                userId,
                                "User logged out"
                        );
                    }
                } catch (Exception e) {
                    log.debug("Error processing refresh token on logout: {}", e.getMessage());
                }
            }

            log.info("User logged out successfully");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
        }
    }


    @Transactional
    public void changePassword(ChangePassword request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadCredentialsException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setLastPasswordChange(Instant.now());

        user.incrementTokenVersion();
        user.invalidateRefreshToken();

        userRepository.save(user);

        auditService.logPasswordChange(user, getClientIp(getCurrentRequest()));

        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    public UserInfoDto getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toUserInfoDto(user);
    }


    private void validateUserCanLogin(User user) {
        if (user.isDeleted()) {
            throw new DisabledException("Account has been deleted");
        }

        if (!user.isActive()) {
            throw new DisabledException("Account is inactive");
        }

        if (user.isAccountLocked()) {
            throw new LockedException("Account is locked");
        }

        if (!user.isEmailVerified()) {
            throw new DisabledException("Email not verified");
        }
    }


    private void handleFailedLogin(User user, HttpServletRequest request) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLocked(true);
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION));

            log.warn("Account locked due to {} failed login attempts: {}", attempts, user.getEmail());

            auditService.logAccountLocked(user, getClientIp(request));
        }

        userRepository.save(user);
    }

    private void resetFailedAttempts(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
    }

    private void updateLastLogin(User user, HttpServletRequest request) {
        user.setLastLogin(Instant.now());
        user.setLastIpAddress(getClientIp(request));
        user.setLastUserAgent(request.getHeader("User-Agent"));

        userRepository.save(user);
    }

    private boolean isUnlockTimeExpired(User user) {
        if (user.getLockedUntil() == null) {
            return false;
        }
        return Instant.now().isAfter(user.getLockedUntil());
    }


    private void unlockAccount(User user) {
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        log.info("Account unlocked: {}", user.getEmail());
    }


    private LoginResult createLoginResult(User user, String accessToken, String refreshToken) {
        return LoginResult.builder()
                .accessCookie(cookieService.createAccessTokenCookie(accessToken))
                .refreshCookie(cookieService.createRefreshTokenCookie(refreshToken))
                .userInfoDto(userMapper.toUserInfoDto(user))
                .expiresIn(3600L)
                .build();
    }


    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private HttpServletRequest getCurrentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
    }
}