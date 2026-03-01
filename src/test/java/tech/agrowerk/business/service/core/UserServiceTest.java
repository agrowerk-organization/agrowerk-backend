package tech.agrowerk.business.service.core;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.agrowerk.application.dto.request.CreateUserRequest;
import tech.agrowerk.application.dto.response.UserResponse;
import tech.agrowerk.application.dto.request.UpdateUserRequest;
import tech.agrowerk.business.service.base.BaseIntegrationTest;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.model.core.Role;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;
import tech.agrowerk.infrastructure.repository.core.RoleRepository;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest extends BaseIntegrationTest {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static UUID testRoleId;
    private static UUID testUserId;
    private static String testUserEmail = "testuser@agrowerk.tech";

    @Autowired
    public UserServiceTest(
            UserService userService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeAll
    static void beforeAll() {
        log.info("PostgreSQL Container started at: {}:{}",
                postgresContainer.getHost(),
                postgresContainer.getFirstMappedPort());
    }

    @BeforeEach
    void setup() {
        if (testRoleId == null) {
            Role role = roleRepository.findByName(RoleType.PRODUCER)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(RoleType.PRODUCER);
                        return roleRepository.save(newRole);
                    });
            testRoleId = role.getId();
            log.info("Test role ready with ID: {}", testRoleId);
        }
    }

    @AfterAll
    static void finalCleanup(
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository) {
        userRepository.deleteAll();
        log.info("Global cleanup: All users deleted");
        roleRepository.deleteAll();
        log.info("Global cleanup: All roles deleted");
    }


    @Test
    @Order(1)
    @DisplayName("1. createUser - Success: persists user with encoded password")
    void testCreateUser_Success() {
        log.info("Running Test 1: createUser happy path");

        CreateUserRequest request = new CreateUserRequest(
                "Test User",
                testUserEmail,
                "Test@1234",
                "Test@1234",
                "(88) 99999-0000",
                "529.982.247-25",
                testRoleId
        );

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUserEmail);
        assertThat(response.name()).isEqualTo("Test User");

        testUserId = response.id();

        User saved = userRepository.findById(testUserId).orElseThrow();
        assertThat(passwordEncoder.matches("Test@1234", saved.getPassword())).isTrue();

        log.info("User created with ID: {}", testUserId);
    }


    @Test
    @Order(2)
    @DisplayName("2. createUser - Conflict: duplicate email throws EntityAlreadyExistsException")
    void testCreateUser_DuplicateEmail_Throws() {
        log.info("Running Test 2: createUser duplicate email");

        CreateUserRequest duplicate = new CreateUserRequest(
                "Another User",
                testUserEmail,
                "Test@1234",
                "Test@1234",
                "(88) 99999-1111",
                "000.000.000-00",
                testRoleId
        );

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        log.info("Duplicate email correctly rejected");
    }


    @Test
    @Order(3)
    @DisplayName("3. findUserById - Success: returns correct user")
    void testFindUserById_Success() {
        log.info("Running Test 3: findUserById");

        UserResponse response = userService.findUserById(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testUserId);
        assertThat(response.email()).isEqualTo(testUserEmail);
    }


    @Test
    @Order(4)
    @DisplayName("4. findUserById - Not found: throws EntityNotFoundException")
    void testFindUserById_NotFound_Throws() {
        log.info("Running Test 4: findUserById not found");

        UUID nonExistent = UUID.randomUUID();

        assertThatThrownBy(() -> userService.findUserById(nonExistent))
                .isInstanceOf(jakarta.persistence.EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(5)
    @DisplayName("5. findUserByEmail - Success: returns correct user")
    void testFindUserByEmail_Success() {
        log.info("Running Test 5: findUserByEmail");

        UserResponse response = userService.findUserByEmail(testUserEmail);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUserEmail);
    }

    @Test
    @Order(6)
    @DisplayName("6. listUsers - Success: returns paginated result with at least one user")
    void testListUsers_Paginated() {
        log.info("Running Test 6: listUsers paginated");

        Page<UserResponse> page = userService.listUsers(PageRequest.of(0, 10));

        assertThat(page).isNotNull();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent())
                .anyMatch(u -> u.email().equals(testUserEmail));

        log.info("Total users found: {}", page.getTotalElements());
    }

    @Test
    @Order(7)
    @DisplayName("7. updateUser - Success: name and telephone are updated")
    void testUpdateUser_NameAndTelephone() {
        mockAuthenticatedUser();
        log.info("Running Test 7: updateUser");

        UpdateUserRequest request = new UpdateUserRequest(
                "Updated Name",
                null,
                "(88) 88888-8888",
                null
        );

        UserResponse response = userService.updateUser(request);

        assertThat(response.name()).isEqualTo("Updated Name");

        User updated = userRepository.findById(testUserId).orElseThrow();
        assertThat(updated.getTelephone()).isEqualTo("(88) 88888-8888");

        log.info("User updated successfully");
    }

    @Test
    @Order(8)
    @DisplayName("8. updateUser - Conflict: email already used by another user throws")
    void testUpdateUser_DuplicateEmail_Throws() {
        mockAuthenticatedUser();
        log.info("Running Test 8: updateUser duplicate email");

        CreateUserRequest secondRequest = new CreateUserRequest(
                "Second User",
                "second@agrowerk.tech",
                "Test@1234",
                "Test@1234",
                "(88) 77777-7777",
                "551.255.530-85",
                testRoleId
        );
        userService.createUser(secondRequest);

        UpdateUserRequest conflictRequest = new UpdateUserRequest(
                null,
                "second@agrowerk.tech",
                null,
                null
        );

        assertThatThrownBy(() -> userService.updateUser(conflictRequest))
                .isInstanceOf(EntityAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        log.info("Duplicate email on update correctly rejected");

        userRepository.findByEmail("second@agrowerk.tech").ifPresent(u -> userRepository.deleteById(u.getId()));
    }


    @Test
    @Order(9)
    @DisplayName("9. deleteUserById - Success: user is soft-deleted with anonymised data")
    void testDeleteUserById_SoftDelete() {
        mockAuthenticatedUser();
        log.info("Running Test 9: deleteUserById");

        userService.deleteUserById();

        User deleted = userRepository.findById(testUserId).orElseThrow();

        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getName()).isEqualTo("deleted");
        assertThat(deleted.getEmail()).startsWith("deleted_").endsWith("@non.local");
        assertThat(deleted.getPassword()).isEqualTo("deleted");
        assertThat(deleted.getCpf()).isNull();
        assertThat(deleted.getTelephone()).isNull();

        log.info("User soft-deleted correctly, anonymised email: {}", deleted.getEmail());
    }


    @Test
    @Order(10)
    @DisplayName("10. Verify Testcontainers are running")
    void testContainerIsRunning() {
        log.info("Running Test 10: Container health check");

        assertThat(postgresContainer.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();

        log.info("PostgreSQL container is running properly");
    }

    private void mockAuthenticatedUser() {
        Map<String, Object> claims = Map.of(
                "userId", testUserId,
                "email", testUserEmail,
                "role", "PRODUCER"
        );

        Jwt jwt = new Jwt(
                "fake-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                claims
        );

        JwtAuthenticationToken auth = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("PRODUCER"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}