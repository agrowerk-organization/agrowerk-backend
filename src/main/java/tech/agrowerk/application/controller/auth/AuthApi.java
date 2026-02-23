package tech.agrowerk.application.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.user.UserInfoDto;

@Tag(name = "Authentication", description = "Endpoints for user login, logout, and token management")
public interface AuthApi {

    @Operation(summary = "User login", description = "Authenticates user and returns access/refresh cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    ResponseEntity<UserInfoDto> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            HttpServletRequest httpServletRequest
    );

    @Operation(summary = "Refresh token", description = "Uses the refresh cookie to generate a new set of access/refresh cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    ResponseEntity<UserInfoDto> refresh(HttpServletRequest httpServletRequest);

    @Operation(summary = "User logout", description = "Invalidates the current session and clears cookies.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout successful")
    })
    ResponseEntity<Void> logout(HttpServletRequest httpServletRequest);

    @Operation(summary = "Change password", description = "Updates user password and logs out from all sessions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid password data")
    })
    ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePassword changePassword);

    @Operation(summary = "Get current user info", description = "Retrieves details of the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User info retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    ResponseEntity<UserInfoDto> getCurrentUser();
}