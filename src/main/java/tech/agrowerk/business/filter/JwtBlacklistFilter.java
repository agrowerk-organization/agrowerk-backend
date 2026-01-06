package tech.agrowerk.business.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import tech.agrowerk.infrastructure.security.JwtService;

import java.io.IOException;
import java.util.List;

@Component
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklistFilter.class);
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;

    private static final List<String> PUBLIC_PATTERNS = List.of(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/favicon.ico",
            "/actuator/health",
            "/actuator/info",
            "/auth/login",
            "/auth/register",
            "/users/register"
    );

    public JwtBlacklistFilter(@Lazy JwtService jwtService, JwtDecoder jwtDecoder) {
        this.jwtService = jwtService;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        boolean isPublic = PUBLIC_PATTERNS.stream()
                .anyMatch(path::startsWith);

        if (isPublic) {
            logger.debug("Skipping JWT filter for public path: {}", path);
        }

        return isPublic;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Jwt jwt = jwtDecoder.decode(token);
                String jti = jwt.getClaimAsString("jti");

                if (jti != null && jwtService.isTokenBlacklisted(jti)) {
                    logger.warn("Blocked blacklisted token (jti: {}) for path: {}",
                            jti, request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\":\"Unauthorized\",\"message\":\"Token has been revoked\"}"
                    );
                    return;
                }
            } catch (JwtException e) {
                logger.debug("Invalid JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}