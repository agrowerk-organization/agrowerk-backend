package tech.agrowerk.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class PermissionsPolicyFilter extends OncePerRequestFilter {

    private static final String PERMISSIONS_POLICY_HEADER = "Permissions-Policy";
    private static final String PERMISSIONS_POLICY_VALUE =
            "geolocation=(), microphone=(), camera=(), payment=(), usb=()";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        response.setHeader(PERMISSIONS_POLICY_HEADER, PERMISSIONS_POLICY_VALUE);
        filterChain.doFilter(request, response);
    }
}