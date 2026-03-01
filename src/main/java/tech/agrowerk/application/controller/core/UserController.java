package tech.agrowerk.application.controller.core;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.agrowerk.application.dto.request.CreateUserRequest;
import tech.agrowerk.application.dto.response.UserResponse;
import tech.agrowerk.application.dto.request.UpdateUserRequest;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.service.core.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @Override
    @GetMapping("/get-user-by-id/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN') or #id.toString() == authentication.principal.claims['userId']")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @Override
    @GetMapping("/get-user-by-email/{email}")
    @PreAuthorize("isAuthenticated() and hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    @Override
    @GetMapping("/get/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @Override
    @GetMapping("/list-users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<UserResponse>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @GetMapping("/search/producers")
    @PreAuthorize("hasAuthority('PRODUCER')")
    public ResponseEntity<Page<UserInfoDto>> searchProducers(
            @RequestParam String query,
            Pageable pageable) {
        return ResponseEntity.ok(userService.searchProducers(query, pageable));
    }

    @Override
    @PutMapping("/update/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUser(UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @Override
    @DeleteMapping("/delete/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUserById();
        return ResponseEntity.noContent().build();
    }
}