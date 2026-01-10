package tech.agrowerk.application.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResponse;
import tech.agrowerk.application.dto.auth.RefreshTokenRequest;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.service.AuthService;
import tech.agrowerk.business.service.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);

        String accessToken = loginResponse.accessToken();
        String refreshToken = loginResponse.refreshToken();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(3600)
                .build();

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(7*24*3600)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.refreshToken(request.refreshToken());

        String newAccessToken = loginResponse.accessToken();
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(3600)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", null)
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();
        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", null)
                .httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(0).build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());

        String token = extractTokenFromCookie(request);
        authService.logout(token);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePassword changePassword, Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String authenticatedEmail = jwt.getSubject();

        authService.changePassword(changePassword);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserInfo());
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
