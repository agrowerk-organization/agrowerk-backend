package tech.agrowerk.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.agrowerk.infrastructure.config.security.SecurityConfig;
import tech.agrowerk.infrastructure.security.services.CookieService;
import tech.agrowerk.infrastructure.security.services.TokenBlacklistService;
import tech.agrowerk.infrastructure.security.validator.JwtUserValidator;

import java.io.IOException;

import java.time.Instant;
import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUserValidator jwtUserValidator;
    private final JwtDecoder jwtDecoder;
    private final CookieService cookieService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean isPublic = Arrays.stream(SecurityConfig.PUBLIC_ENDPOINTS)
                .anyMatch(pattern -> {
                    String regex = pattern.replace("/**", ".*").replace("/*", "/[^/]*");
                    return path.matches(regex);
                });

        if (isPublic) {
            log.debug("Skipping JWT blacklist filter for public path: {}", path);
        }

        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = cookieService.extractAccessToken(request);

            if (token != null) {
                Jwt jwt = jwtDecoder.decode(token);
                String jti = jwt.getClaimAsString("jti");

                if (jti != null && tokenBlacklistService.isBlacklisted(jti)) {
                    log.warn("Blocked blacklisted token: jti={}, path={}", jti, request.getRequestURI());
                    sendUnauthorizedResponse(response, "Token has been revoked");
                    return;
                }

                try {
                    jwtUserValidator.validateToken(jwt);
                    log.debug("Token validated successfully for path: {}", request.getRequestURI());
                } catch (Exception e) {
                    log.warn("Token validation failed: {} - Path: {}", e.getMessage(), request.getRequestURI());
                    sendUnauthorizedResponse(response, e.getMessage());
                    return;
                }

                request.setAttribute("JWT_TOKEN", token);
                request.setAttribute("JWT_JTI", jti);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.warn("Invalid JWT token: {} - Path: {}", e.getMessage(), request.getRequestURI());
            sendUnauthorizedResponse(response, "Invalid token");
        } catch (Exception e) {
            log.error("Error in JWT blacklist filter", e);
            sendUnauthorizedResponse(response, "Authentication error");
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String errorResponse = String.format(
                "{\"error\":\"Unauthorized\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
                message,
                Instant.now().toString()
        );

        response.getWriter().write(errorResponse);
    }
}