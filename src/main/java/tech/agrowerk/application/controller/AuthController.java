package tech.agrowerk.application.controller;

import jakarta.validation.Valid;
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

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        LoginResponse loginResponse = authService.refreshToken(request.refreshToken());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
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
}
