package tech.agrowerk.application.controller.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.agrowerk.application.dto.auth.ForgotPasswordRequest;
import tech.agrowerk.application.dto.auth.ResetPasswordRequest;
import tech.agrowerk.application.dto.auth.ValidateResetTokenRequest;
import tech.agrowerk.business.service.auth.PasswordResetService;

@RestController
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/validate")
    public ResponseEntity<Void> validateResetToken(@Valid @RequestBody ValidateResetTokenRequest request) {
        passwordResetService.validateToken(request.token());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

}
