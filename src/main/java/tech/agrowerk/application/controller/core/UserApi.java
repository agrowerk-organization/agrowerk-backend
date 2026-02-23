package tech.agrowerk.application.controller.core;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import tech.agrowerk.application.dto.crud.create.CreateUserRequest;
import tech.agrowerk.application.dto.crud.get.UserResponse;
import tech.agrowerk.application.dto.crud.update.UpdateUserRequest;

import java.util.UUID;

@Tag(name = "Users", description = "Operations for user profile management and administrative control")
public interface UserApi {

    @Operation(summary = "Register new user", description = "Creates a new user in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided")
    })
    ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request);

    @Operation(summary = "Get user by ID", description = "Retrieves details of a specific user. Access restricted to owners or admins.")
    ResponseEntity<UserResponse> getUserById(UUID id);

    @Operation(summary = "Get user by email", description = "Retrieves user details via email address. Admin only.")
    ResponseEntity<UserResponse> getUserByEmail(String email);

    @Operation(summary = "Get current user profile", description = "Returns the profile data of the currently authenticated user.")
    ResponseEntity<UserResponse> getCurrentUser();

    @Operation(summary = "List all users", description = "Returns a paginated list of all registered users. Admin only.")
    ResponseEntity<Page<UserResponse>> listUsers(@Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Update current user", description = "Updates the profile information of the logged-in user.")
    ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request);

    @Operation(summary = "Delete current user", description = "Performs a self-deletion of the authenticated user's account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully")
    })
    ResponseEntity<Void> deleteUser();
}