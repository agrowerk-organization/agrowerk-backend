/*package tech.agrowerk.application.controller.core;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Email Verification", description = "Endpoints for email ownership validation and token management")
public interface EmailVerificationApi {

    @Operation(summary = "Resend verification email", description = "Triggers a new verification email to the user.")
    @ApiResponse(responseCode = "202", description = "Email request accepted")
    ResponseEntity<Void> resendVerification(@RequestParam String email);

    @Operation(summary = "Verify email token", description = "Validates the token sent to the user's email to activate the account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    ResponseEntity<String> verifyEmail(@RequestParam String token);
} */