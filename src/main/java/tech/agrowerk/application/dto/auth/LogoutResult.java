package tech.agrowerk.application.dto.auth;

import org.springframework.http.ResponseCookie;

public record LogoutResult(
    ResponseCookie accessCookie,
    ResponseCookie refreshCookie
) {}
