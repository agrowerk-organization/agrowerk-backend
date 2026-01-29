package tech.agrowerk.infrastructure.security.services;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CookieService {

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.cookie.secure:true}")
    private boolean secureCookies;

    @Value("${app.cookie.same-site:Strict}")
    private String sameSite;

    @Value("${security.jwt.access-token.expiration:3600}")
    private Long accessTokenExpiration;

    @Value("${security.jwt.refresh-token.expiration:604800}")
    private Long refreshTokenExpiration;

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    public static final String REMEMBER_ME_COOKIE = "rememberMe";
    public static final String CSRF_TOKEN_COOKIE = "XSRF-TOKEN";


    public ResponseCookie createAccessTokenCookie(String token) {
        return createSecureCookie(
                ACCESS_TOKEN_COOKIE,
                token,
                accessTokenExpiration.intValue(),
                "/",
                "Access token cookie"
        );
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createSecureCookie(
                REFRESH_TOKEN_COOKIE,
                token,
                refreshTokenExpiration.intValue(),
                "/auth",
                "Refresh token cookie"
        );
    }

    public ResponseCookie createSecureCookie(String name, String value, int maxAgeSeconds, String path, String description) {
        log.debug("Creating secure cookie: {} - {}", name, description);

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secureCookies)
                .path(path)
                .maxAge(maxAgeSeconds)
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public ResponseCookie createRememberMeCookie(String userId) {
        return createSecureCookie(
                REMEMBER_ME_COOKIE,
                userId,
                30 * 24 * 3600,
                "/",
                "Remember me cookie"
        );
    }

    public ResponseCookie createCsrfCookie(String csrfToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(CSRF_TOKEN_COOKIE, csrfToken)
                .httpOnly(false)
                .secure(secureCookies)
                .path("/")
                .maxAge(3600)
                .sameSite(sameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return deleteCookie(ACCESS_TOKEN_COOKIE, "/");
    }


    public ResponseCookie deleteRefreshTokenCookie() {
        return deleteCookie(REFRESH_TOKEN_COOKIE, "/auth");
    }


    public ResponseCookie deleteCookie(String name, String path) {
        log.debug("Deleting cookie: {} - path: {}", name, path);

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookies)
                .path(path)
                .maxAge(0);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }


    public List<ResponseCookie> deleteAllAuthCookies() {
        return List.of(
                deleteAccessTokenCookie(),
                deleteRefreshTokenCookie(),
                deleteCookie(REMEMBER_ME_COOKIE, "/"),
                deleteCookie(CSRF_TOKEN_COOKIE, "/")
        );
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    log.debug("Cookie found: {}", cookieName);
                    return cookie.getValue();
                }
            }
        }
        log.debug("Cookie not found: {}", cookieName);
        return null;
    }

    public String extractAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("Access token found in Authorization header");
            return authHeader.substring(7);
        }

        String cookieToken = getCookieValue(request, ACCESS_TOKEN_COOKIE);
        if (cookieToken != null) {
            log.debug("Access token found in cookie");
            return cookieToken;
        }

        log.debug("No access token found");
        return null;
    }


    public String extractRefreshToken(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    public boolean hasCookie(HttpServletRequest request, String cookieName) {
        return getCookieValue(request, cookieName) != null;
    }

    public void addCookiesToResponse(HttpServletResponse response, ResponseCookie... cookies) {
        for (ResponseCookie cookie : cookies) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            showDebugMessage(cookie);
        }
    }

    public void addCookiesToResponse(HttpServletResponse response, List<ResponseCookie> cookies) {
        cookies.forEach(cookie -> {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            showDebugMessage(cookie);
        });
    }

    private void showDebugMessage(ResponseCookie cookie) {
        log.debug("Cookie added to response: {}", cookie.getName());
    }

    @PostConstruct
    public void validateConfiguration() {
        log.info("Cookie configuration:");
        log.info("  - Domain: {}", cookieDomain.isBlank() ? "not set (using request domain)" : cookieDomain);
        log.info("  - Secure: {}", secureCookies);
        log.info("  - SameSite: {}", sameSite);
        log.info("  - Access Token TTL: {}s", accessTokenExpiration);
        log.info("  - Refresh Token TTL: {}s", refreshTokenExpiration);

        if (!secureCookies) {
            log.warn("WARNING: Secure cookies are DISABLED. This should ONLY be used in development!");
        }

        if (!"Strict".equals(sameSite) && !"Lax".equals(sameSite) && !"None".equals(sameSite)) {
            log.error("Invalid SameSite value: {}. Must be Strict, Lax, or None", sameSite);
            throw new IllegalStateException("Invalid SameSite configuration");
        }

        if ("None".equals(sameSite) && !secureCookies) {
            log.error("SameSite=None requires Secure=true");
            throw new IllegalStateException("SameSite=None requires secure cookies");
        }
    }
}