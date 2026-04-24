package tech.agrowerk.application.controller.core;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.core.AddAddressRequest;
import tech.agrowerk.application.dto.request.core.CreateUserRequest;
import tech.agrowerk.application.dto.request.core.UpdateAddressRequest;
import tech.agrowerk.application.dto.response.core.AddressResponse;
import tech.agrowerk.application.dto.response.core.UserProfileResponse;
import tech.agrowerk.application.dto.response.core.UserResponse;
import tech.agrowerk.application.dto.request.core.UpdateUserRequest;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.service.core.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("add-address/me/address/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable UUID userId, @Valid @RequestBody AddAddressRequest request) {

        return ResponseEntity.ok(userService.addAddress(userId, request));
    }

    @PatchMapping("update-address/me/address/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable UUID userId, @Valid @RequestBody UpdateAddressRequest request) {
        return ResponseEntity.ok(userService.updateAddress(userId, request));
    }

    @GetMapping("/get-user-by-id/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_ADMIN') or #id.toString() == authentication.principal.claims['userId']")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping("/get-user-by-email/{email}")
    @PreAuthorize("isAuthenticated() and hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.findUserByEmail(email));
    }

    @GetMapping("/profile/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }

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

    @PutMapping("/update/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateUser(UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @DeleteMapping("/delete/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUserById();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadAvatar(@RequestParam("file")MultipartFile file) {
        return ResponseEntity.ok(userService.uploadAvatar(file));
    }
}