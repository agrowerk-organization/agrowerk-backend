package tech.agrowerk.infrastructure.security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.security.details.CustomUserDetails;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Value("${security.jwt.access-token.expiration:3600}")
    private Long accessTokenExpiration;

    @Value("${security.jwt.refresh-token.expiration:604800}")
    private Long refreshTokenExpiration;

    @Value("${security.jwt.issuer}")
    private String issuer;

    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        return generateAccessToken(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRole().name(),
                userDetails.getTokenVersion()
        );
    }

    public String generateTokenFromUser(User user) {
        validateUserCanLogin(user);

        return generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().getName().name(),
                user.getTokenVersion()
        );
    }

    private String generateAccessToken(UUID userId, String email, String role, Integer tokenVersion) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiration))
                .subject(email)
                .claim("userId", userId)
                .claim("email", email)
                .claim("role", role)
                .claim("tv", tokenVersion)
                .claim("type", "access")
                .claim("jti", jti)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(User user) {
        validateUserCanLogin(user);

        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshTokenExpiration))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", "refresh")
                .claim("jti", jti)
                .claim("tv", user.getTokenVersion())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Jwt decodeAndValidateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            String jti = jwt.getClaimAsString("jti");
            if (tokenBlacklistService.isBlacklisted(jti)) {
                throw new AccessDeniedException("Token has been revoked");
            }

            Instant expiration = jwt.getExpiresAt();
            if (expiration != null && Instant.now().isAfter(expiration)) {
                throw new AccessDeniedException("Token has expired");
            }

            UUID userId = jwt.getClaim("userId");
            Integer tokenVersion = jwt.getClaim("tv");

            if (userId != null && tokenVersion != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new AccessDeniedException("User not found"));

                if (tokenVersion != user.getTokenVersion()) {
                    throw new AccessDeniedException("Token has been revoked (version mismatch)");
                }

                validateUserCanLogin(user);
            }

            return jwt;

        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new AccessDeniedException("Invalid token");
        }
    }


    public void invalidateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String jti = jwt.getClaimAsString("jti");
            Instant expiration = jwt.getExpiresAt();

            if (jti != null && expiration != null) {
                long ttl = expiration.getEpochSecond() - Instant.now().getEpochSecond();
                if (ttl > 0) {
                    tokenBlacklistService.blacklistToken(jti, ttl);
                    log.info("Token invalidado: jti={}", jti);
                }
            }
        } catch (JwtException e) {
            log.warn("Tentativa de invalidar token inválido: {}", e.getMessage());
        }
    }

    public void invalidateAllUserTokens(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.incrementTokenVersion();
        userRepository.save(user);

        tokenBlacklistService.blacklistAllUserTokens(userId);
        log.warn("Todos os tokens do usuário {} foram invalidados", userId);
    }


    public Long extractUserId(Jwt jwt) {
        return jwt.getClaim("userId");
    }

    public String extractUsername(Jwt jwt) {
        return jwt.getSubject();
    }

    public String extractTokenType(Jwt jwt) {
        return jwt.getClaimAsString("type");
    }

    public Integer extractTokenVersion(Jwt jwt) {
        return jwt.getClaim("tv");
    }

    private void validateUserCanLogin(User user) {
        if (user.isDeleted()) {
            throw new DisabledException("User account has been deleted");
        }

        if (!user.isActive()) {
            throw new DisabledException("User account is inactive");
        }

        if (user.isAccountLocked()) {
            throw new LockedException("User account is locked");
        }

        if (!user.isEmailVerified()) {
            throw new DisabledException("Email not verified");
        }
    }

    public boolean isRefreshToken(Jwt jwt) {
        String type = extractTokenType(jwt);
        return "refresh".equals(type);
    }

    public boolean isAccessToken(Jwt jwt) {
        String type = extractTokenType(jwt);
        return "access".equals(type);
    }
}