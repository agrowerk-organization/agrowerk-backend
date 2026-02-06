package tech.agrowerk.infrastructure.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.agrowerk.infrastructure.security.services.RateLimitService;

@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        String ip = getClientIp(request);
        String path = request.getRequestURI();

        if (isSensitiveEndpoint(path)) {
            if (!rateLimitService.isAllowedForSensitiveEndpoint(ip, path)) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\":\"Too many requests. Please try again later.\"}"
                );
                log.warn("Rate limit excedido - IP: {}, Path: {}", ip, path);
                return false;
            }
        }

        // Rate limit geral por IP
        if (!rateLimitService.isAllowedByIp(ip)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests from your IP. Please slow down.\"}"
            );
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isSensitiveEndpoint(String path) {
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.startsWith("/auth/forgot-password") ||
                path.startsWith("/auth/reset-password") ||
                path.startsWith("/auth/verify-email");
    }
}
