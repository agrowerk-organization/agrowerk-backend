package tech.agrowerk.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.model.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${security.jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${security.jwt.issuer}")
    private String issuer;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        assert userDetails != null;
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiration))
                .subject(authentication.getName())
                .claim("userId", userDetails.getId())
                .claim("email", userDetails.getUsername())
                .claim("role", userDetails.getRole().name())
               // .claim("tv", userDetails.getTokenVersion())
                .claim("jti", UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateTokenFromUser(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTokenExpiration))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getName())
                .claim("tv", user.getTokenVersion())
                .claim("jti", UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(String username) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .subject(username)
                .claim("type", "refresh")
                .claim("jti", UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshTokenFromUser(User user) {
        return generateRefreshToken(user.getEmail());
    }

    public void invalidateToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        String jti = jwt.getClaimAsString("jti");
        blacklistedTokens.add(jti);
    }

    public boolean isTokenBlacklisted(String jti) {
        return blacklistedTokens.contains(jti);
    }

    public Jwt decodeToken(String token) {
        return jwtDecoder.decode(token);
    }

    public Long extractUserId(Jwt jwt) {
        return jwt.getClaim("userId");
    }

    public String extractUsername(Jwt jwt) {
        return jwt.getSubject();
    }
}