package tech.agrowerk.business.interceptors;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tech.agrowerk.infrastructure.security.RateLimitService;

import java.io.IOException;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String clientIp = getClientIp(request);
        Bucket bucket = rateLimitService.resolveBucket(clientIp);

        if (!bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests. Try again later.\"}"
            );
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0].trim();
    }
}
