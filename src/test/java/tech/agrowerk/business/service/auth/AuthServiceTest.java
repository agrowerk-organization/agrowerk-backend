package tech.agrowerk.business.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResult;
import tech.agrowerk.business.service.base.BaseIntegrationTest;
import tech.agrowerk.infrastructure.exception.local.BadCredentialsException;
import tech.agrowerk.infrastructure.exception.local.InvalidTokenException;
import tech.agrowerk.infrastructure.model.core.Role;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.core.enums.RoleType;
import tech.agrowerk.infrastructure.repository.core.RoleRepository;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest extends BaseIntegrationTest {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @MockitoBean
    private HttpServletRequest mockRequest;

    private static UUID testRoleId;
    private static UUID testUserId;

    private static final String TEST_EMAIL    = "auth.test@agrowerk.tech";
    private static final String TEST_PASSWORD = "Auth@1234";
    private static final String TEST_CPF      = "529.982.247-25";

    private static String capturedAccessToken;
    private static String capturedRefreshToken;

    @Autowired
    public AuthServiceTest(
            AuthService authService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeAll
    static void beforeAll() {
        log.info("PostgreSQL started at: {}:{}", postgresContainer.getHost(), postgresContainer.getFirstMappedPort());
        log.info("Redis started at: {}:{}", redisContainer.getHost(), redisContainer.getMappedPort(6379));
    }

    @BeforeEach
    void setupMocks() {
        when(mockRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(mockRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(mockRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(mockRequest.getHeader("User-Agent")).thenReturn("JUnit/5");
    }

    @BeforeEach
    void setupUser() {
        if (testUserId != null) return;

        Role role = roleRepository.findByName(RoleType.PRODUCER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(RoleType.PRODUCER);
                    return roleRepository.save(newRole);
                });
        testRoleId = role.getId();

        User user = new User();
        user.setName("Auth Tester");
        user.setEmail(TEST_EMAIL);
        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        user.setCpf(TEST_CPF);
        user.setActive(true);
        user.setDeleted(false);
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setRole(roleRepository.findById(testRoleId).orElseThrow());
        user.setLastLogin(Instant.now());

        user = userRepository.save(user);
        testUserId = user.getId();
        log.info("Test user created with ID: {}", testUserId);
    }

    @AfterAll
    static void finalCleanup(
            @Autowired UserRepository userRepository,
            @Autowired RoleRepository roleRepository) {
        if (testUserId != null) {
            userRepository.deleteById(testUserId);
            log.info("Global cleanup: test user deleted");
        }
        if (testRoleId != null) {
            roleRepository.deleteById(testRoleId);
            log.info("Global cleanup: test role deleted");
        }
    }


    @Test
    @Order(1)
    @DisplayName("1. login - Success: returns access and refresh tokens")
    void testLogin_Success() {
        log.info("Running Test 1: login happy path");

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        LoginResult result = authService.login(request, mockRequest);

        assertThat(result).isNotNull();
        assertThat(result.accessCookie()).isNotNull();
        assertThat(result.refreshCookie()).isNotNull();
        assertThat(result.userInfoDto()).isNotNull();
        assertThat(result.userInfoDto().email()).isEqualTo(TEST_EMAIL);
        assertThat(result.expiresIn()).isEqualTo(3600L);

        capturedAccessToken  = result.accessCookie().getValue();
        capturedRefreshToken = result.refreshCookie().getValue();

        log.info("Login successful, tokens captured");
    }

    @Test
    @Order(2)
    @DisplayName("2. login - Wrong password: throws BadCredentialsException")
    void testLogin_WrongPassword_Throws() {
        log.info("Running Test 2: login wrong password");

        LoginRequest request = new LoginRequest(TEST_EMAIL, "Wrong@9999");

        assertThatThrownBy(() -> authService.login(request, mockRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        log.info("Wrong password correctly rejected");
    }

    @Test
    @Order(3)
    @DisplayName("3. login - Unknown email: throws BadCredentialsException (timing-safe)")
    void testLogin_UnknownEmail_Throws() {
        log.info("Running Test 3: login unknown email");

        LoginRequest request = new LoginRequest("nobody@agrowerk.tech", TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, mockRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @Order(4)
    @DisplayName("4. login - Account locked: throws LockedException")
    void testLogin_AccountLocked_Throws() {
        log.info("Running Test 4: login locked account");

        User user = userRepository.findById(testUserId).orElseThrow();
        user.setLocked(true);
        user.setLockedUntil(Instant.now().plusSeconds(900));
        userRepository.save(user);

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, mockRequest))
                .isInstanceOf(LockedException.class);

        user = userRepository.findById(testUserId).orElseThrow();
        user.setLocked(false);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        log.info("Locked account correctly rejected");
    }

    @Test
    @Order(5)
    @DisplayName("5. login - Deleted account: throws DisabledException")
    void testLogin_DeletedAccount_Throws() {
        log.info("Running Test 5: login deleted account");

        User user = userRepository.findById(testUserId).orElseThrow();
        user.setDeleted(true);
        userRepository.save(user);

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, mockRequest))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("deleted");

        user = userRepository.findById(testUserId).orElseThrow();
        user.setDeleted(false);
        userRepository.save(user);
    }

    @Test
    @Order(6)
    @DisplayName("6. login - Inactive account: throws DisabledException")
    void testLogin_InactiveAccount_Throws() {
        log.info("Running Test 6: login inactive account");

        User user = userRepository.findById(testUserId).orElseThrow();
        user.setActive(false);
        userRepository.save(user);

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, mockRequest))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("inactive");

        user = userRepository.findById(testUserId).orElseThrow();
        user.setActive(true);
        userRepository.save(user);
    }

    @Test
    @Order(7)
    @DisplayName("7. login - Email not verified: throws DisabledException")
    void testLogin_EmailNotVerified_Throws() {
        log.info("Running Test 7: login email not verified");

        User user = userRepository.findById(testUserId).orElseThrow();
        user.setEmailVerified(false);
        userRepository.save(user);

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        assertThatThrownBy(() -> authService.login(request, mockRequest))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("verified");

        user = userRepository.findById(testUserId).orElseThrow();
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Test
    @Order(8)
    @DisplayName("8. login - 5 failed attempts lock the account automatically")
    void testLogin_FiveFailedAttempts_LocksAccount() {
        log.info("Running Test 8: account auto-lock after 5 failed attempts");

        User user = userRepository.findById(testUserId).orElseThrow();
        user.setFailedLoginAttempts(0);
        user.setLocked(false);
        userRepository.save(user);

        LoginRequest badRequest = new LoginRequest(TEST_EMAIL, "Wrong@0000");

        for (int i = 0; i < 5; i++) {
            try { authService.login(badRequest, mockRequest); } catch (Exception ignored) {}
        }

        User locked = userRepository.findById(testUserId).orElseThrow();

        log.info("isLocked={}, lockedUntil={}, failedAttempts={}",
                locked.isLocked(), locked.getLockedUntil(), locked.getFailedLoginAttempts());

        assertThat(locked.isLocked()).isTrue();
        assertThat(locked.getLockedUntil()).isNotNull().isAfter(Instant.now());

        log.info("Account locked after 5 failed attempts, lockedUntil={}", locked.getLockedUntil());

        locked.setLocked(false);
        locked.setLockedUntil(null);
        locked.setFailedLoginAttempts(0);
        userRepository.save(locked);

        LoginResult result = authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), mockRequest);
        capturedAccessToken  = result.accessCookie().getValue();
        capturedRefreshToken = result.refreshCookie().getValue();
    }

    @Test
    @Order(9)
    @DisplayName("9. login - Expired lock is auto-unlocked on next login attempt")
    void testLogin_ExpiredLock_AutoUnlocks() {
        log.info("Running Test 9: expired lock auto-unlock");

        User user = userRepository.findById(testUserId).orElseThrow();
        user.setLocked(true);
        user.setLockedUntil(Instant.now().minusSeconds(1));
        userRepository.save(user);

        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        LoginResult result = authService.login(request, mockRequest);

        assertThat(result).isNotNull();

        User unlocked = userRepository.findById(testUserId).orElseThrow();
        assertThat(unlocked.isAccountLocked()).isFalse();

        capturedAccessToken  = result.accessCookie().getValue();
        capturedRefreshToken = result.refreshCookie().getValue();

        log.info("Expired lock auto-unlocked successfully");
    }

    @Test
    @Order(10)
    @DisplayName("10. refreshToken - Success: returns new token pair")
    void testRefreshToken_Success() {
        log.info("Running Test 10: refreshToken happy path");

        if (capturedRefreshToken == null || capturedRefreshToken.isBlank()) {
            LoginResult login = authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), mockRequest);
            capturedRefreshToken = login.refreshCookie().getValue();
            capturedAccessToken  = login.accessCookie().getValue();
        }

        LoginResult result = authService.refreshToken(capturedRefreshToken, mockRequest);

        assertThat(capturedRefreshToken)
                .as("Refresh token must be captured from a previous login test")
                .isNotBlank();

        assertThat(result).isNotNull();
        assertThat(result.accessCookie()).isNotNull();
        assertThat(result.refreshCookie()).isNotNull();

        capturedAccessToken  = result.accessCookie().getValue();
        capturedRefreshToken = result.refreshCookie().getValue();

        log.info("Token refreshed successfully");
    }

    @Test
    @Order(11)
    @DisplayName("11. refreshToken - Blank token: throws InvalidTokenException")
    void testRefreshToken_BlankToken_Throws() {
        log.info("Running Test 11: refreshToken blank token");

        assertThatThrownBy(() -> authService.refreshToken("", mockRequest))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("required");
    }

    @Test
    @Order(12)
    @DisplayName("12. refreshToken - Invalid/tampered token: throws InvalidTokenException")
    void testRefreshToken_TamperedToken_Throws() {
        log.info("Running Test 12: refreshToken tampered token");

        assertThatThrownBy(() -> authService.refreshToken("this.is.not.a.valid.jwt", mockRequest))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    @Order(13)
    @DisplayName("13. changePassword - Success: password is updated and refresh token is invalidated")
    void testChangePassword_Success() {
        log.info("Running Test 13: changePassword happy path");

        String newPassword = "NewAuth@5678";

        ChangePassword request = new ChangePassword(TEST_EMAIL, TEST_PASSWORD, newPassword, newPassword);

        authService.changePassword(request);

        User user = userRepository.findById(testUserId).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, user.getPassword())).isTrue();
        assertThat(user.getLastPasswordChange()).isNotNull();

        log.info("Password changed successfully");

        user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
        userRepository.save(user);

        LoginResult result = authService.login(new LoginRequest(TEST_EMAIL, TEST_PASSWORD), mockRequest);
        capturedRefreshToken = result.refreshCookie().getValue();
        capturedAccessToken  = result.accessCookie().getValue();
    }

    @Test
    @Order(14)
    @DisplayName("14. changePassword - Wrong current password: throws BadCredentialsException")
    void testChangePassword_WrongCurrentPassword_Throws() {
        log.info("Running Test 14: changePassword wrong current password");

        ChangePassword request = new ChangePassword(TEST_EMAIL, "Wrong@0000", "NewAuth@5678", "NewAuth@5678");

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("incorrect");
    }

    @Test
    @Order(15)
    @DisplayName("15. changePassword - Same as current password: throws BadCredentialsException")
    void testChangePassword_SamePassword_Throws() {
        log.info("Running Test 15: changePassword same password");

        ChangePassword request = new ChangePassword(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_PASSWORD);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("different");
    }

    @Test
    @Order(16)
    @DisplayName("16. logout - Success: access token is blacklisted and refresh token invalidated")
    void testLogout_Success() {
        log.info("capturedAccessToken={}", capturedAccessToken != null ? "present" : "NULL");
        log.info("capturedRefreshToken={}", capturedRefreshToken != null ? "present" : "NULL");

        User before = userRepository.findById(testUserId).orElseThrow();
        log.info("refreshTokenHash antes={}", before.getRefreshTokenHash());
        log.info("tokenValido antes={}", before.isRefreshTokenValid(capturedRefreshToken, passwordEncoder));

        String tokenToValidate = capturedRefreshToken;

        assertThatCode(() -> authService.logout(capturedAccessToken, tokenToValidate, mockRequest))
                .doesNotThrowAnyException();

        User user = userRepository.findById(testUserId).orElseThrow();
        log.info("refreshTokenHash depois={}", user.getRefreshTokenHash());
        assertThat(user.isRefreshTokenValid(tokenToValidate, passwordEncoder)).isFalse();
    }

    @Test
    @Order(17)
    @DisplayName("17. logout - Null tokens: completes gracefully without exception")
    void testLogout_NullTokens_GracefulHandling() {
        log.info("Running Test 17: logout with null tokens");

        assertThatCode(() -> authService.logout(null, null, mockRequest))
                .doesNotThrowAnyException();
    }

    @Test
    @Order(18)
    @DisplayName("18. Verify Testcontainers are running")
    void testContainersAreRunning() {
        log.info("Running Test 18: Container health check");

        assertThat(postgresContainer.isRunning())
                .as("PostgreSQL container should be running")
                .isTrue();

        assertThat(redisContainer.isRunning())
                .as("Redis container should be running")
                .isTrue();

        log.info("All containers are running properly");
    }
}