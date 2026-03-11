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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import tech.agrowerk.application.dto.request.create.AddAddressRequest;
import tech.agrowerk.application.dto.request.create.CreateUserRequest;
import tech.agrowerk.application.dto.request.update.UpdateAddressRequest;
import tech.agrowerk.application.dto.response.AddressResponse;
import tech.agrowerk.application.dto.response.UserResponse;
import tech.agrowerk.application.dto.request.update.UpdateUserRequest;
import tech.agrowerk.application.dto.user.UserInfoDto;

import java.util.UUID;

@Tag(name = "Users", description = "Operations for user profile management and administrative control")
public interface UserApi {

    @Operation(summary = "Register new user", description = "Creates a new user in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided")
    })
    ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request);

    @Operation(summary = "Add address for user", description = "Creates a address for user in the system.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided")
    })
    ResponseEntity<AddressResponse> addAddress(@PathVariable UUID userId, @Valid @RequestBody AddAddressRequest request);

    @Operation(summary = "Update address for user", description = "Updates the address of the logged-in user.")
    ResponseEntity<AddressResponse> updateAddress(@PathVariable UUID userId, @Valid @RequestBody UpdateAddressRequest request);

    @Operation(summary = "Get user by ID", description = "Retrieves details of a specific user. Access restricted to owners or admins.")
    ResponseEntity<UserResponse> getUserById(UUID id);

    @Operation(summary = "Get user by email", description = "Retrieves user details via email address. Admin only.")
    ResponseEntity<UserResponse> getUserByEmail(String email);

    @Operation(summary = "Get current user profile", description = "Returns the profile data of the currently authenticated user.")
    ResponseEntity<UserResponse> getCurrentUser();

    @Operation(summary = "List all users", description = "Returns a paginated list of all registered users. Admin only.")
    ResponseEntity<Page<UserResponse>> listUsers(@Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Search producers", description = "Returns a paginated list of producers filtered by name or email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producers found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    ResponseEntity<Page<UserInfoDto>> searchProducers(
            @Parameter(description = "Name or email to search") @RequestParam String query,
            Pageable pageable
    );

    @Operation(summary = "Update current user", description = "Updates the profile information of the logged-in user.")
    ResponseEntity<UserResponse> updateUser(@Valid @RequestBody UpdateUserRequest request);

    @Operation(summary = "Delete current user", description = "Performs a self-deletion of the authenticated user's account.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully")
    })
    ResponseEntity<Void> deleteUser();
}