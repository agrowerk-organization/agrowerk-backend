package tech.agrowerk.application.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResult;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.service.auth.AuthService;
import tech.agrowerk.infrastructure.security.services.CookieService;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final CookieService cookieService;

    public AuthController(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserInfoDto> login(LoginRequest loginRequest, HttpServletResponse response, HttpServletRequest httpServletRequest) {
        log.info("Login attempt for email: {}", loginRequest.email());
        LoginResult loginResult = authService.login(loginRequest, httpServletRequest);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, loginResult.accessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookie().toString())
                .body(loginResult.userInfoDto());
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<UserInfoDto> refresh(HttpServletRequest httpServletRequest) {
        String refreshToken = cookieService.extractRefreshToken(httpServletRequest);

        if (refreshToken == null) {
            log.warn("Refresh token not found in cookies");
            return ResponseEntity.status(401).build();
        }

        LoginResult loginResult = authService.refreshToken(refreshToken, httpServletRequest);
        log.info("Token refreshed successfully for user: {}", loginResult.userInfoDto().email());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, loginResult.accessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, loginResult.refreshCookie().toString())
                .body(loginResult.userInfoDto());
    }

    @Override
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(HttpServletRequest httpServletRequest) {
        String accessToken = cookieService.extractAccessToken(httpServletRequest);
        String refreshToken = cookieService.extractRefreshToken(httpServletRequest);

        authService.logout(accessToken, refreshToken, httpServletRequest);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieService.deleteAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.deleteRefreshTokenCookie().toString())
                .build();
    }

    @Override
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(ChangePassword changePassword) {
        log.info("Password change request for email: {}", changePassword.email());
        authService.changePassword(changePassword);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieService.deleteAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.deleteRefreshTokenCookie().toString())
                .build();
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserInfoDto> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUserInfo());
    }
}