package tech.agrowerk.application.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.agrowerk.application.controller.base.BaseControllerTest;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResult;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.service.auth.AuthService;
import tech.agrowerk.infrastructure.config.TestSecurityConfig;
import tech.agrowerk.infrastructure.exception.global.AdvancedGlobalExceptionHandler;
import tech.agrowerk.infrastructure.exception.local.BadCredentialsException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, AdvancedGlobalExceptionHandler.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    @Override
    public void setUp() {
        super.setUpSecurity();
        ResponseCookie mockCookie = ResponseCookie.from("dummy", "value").build();
        when(cookieService.deleteAccessTokenCookie()).thenReturn(mockCookie);
        when(cookieService.deleteRefreshTokenCookie()).thenReturn(mockCookie);
        when(cookieService.extractRefreshToken(any())).thenReturn("valid-token");
        when(cookieService.extractAccessToken(any())).thenReturn("valid-access");
    }

    private LoginResult buildLoginResult(String email) {
        ResponseCookie accessCookie  = ResponseCookie.from("access_token", "access.jwt.token").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "refresh.jwt.token").build();
        UserInfoDto userInfo = new UserInfoDto(USER_ID,"Test User", email);
        return LoginResult.builder()
                .accessCookie(accessCookie)
                .refreshCookie(refreshCookie)
                .userInfoDto(userInfo)
                .expiresIn(3600L)
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("1. POST /auth/login - 200 OK with Set-Cookie headers on valid credentials")
    void testLogin_ValidCredentials_Returns200WithCookies() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@agrowerk.tech", "Auth@1234");
        LoginResult  loginResult  = buildLoginResult("user@agrowerk.tech");

        when(authService.login(any(), any())).thenReturn(loginResult);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.email").value("user@agrowerk.tech"));
    }

    @Test
    @Order(2)
    @DisplayName("2. POST /auth/login - 401 when AuthService throws BadCredentialsException")
    void testLogin_BadCredentials_Returns401() throws Exception {
        LoginRequest loginRequest = new LoginRequest("user@agrowerk.tech", "TheAuth@5678");

        when(authService.login(any(), any())).thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("3. POST /auth/login - 400 when body is missing required fields")
    void testLogin_MissingBody_Returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("4. POST /auth/refresh - 200 OK with new cookies when refresh token is valid")
    void testRefresh_ValidToken_Returns200() throws Exception {
        LoginResult loginResult = buildLoginResult("user@agrowerk.tech");

        when(cookieService.extractRefreshToken(any())).thenReturn("refresh.jwt.token");
        when(authService.refreshToken(anyString(), any())).thenReturn(loginResult);

        mockMvc.perform(post("/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.email").value("user@agrowerk.tech"));
    }

    @Test
    @Order(5)
    @DisplayName("5. POST /auth/refresh - 401 when refresh token cookie is absent")
    void testRefresh_MissingToken_Returns401() throws Exception {
        when(cookieService.extractRefreshToken(any())).thenReturn(null);

        mockMvc.perform(post("/auth/refresh")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("6. POST /auth/logout - 204 No Content and clears cookies")
    @WithMockUser
    void testLogout_Authenticated_Returns204WithClearedCookies() throws Exception {
        ResponseCookie deletedAccess  = ResponseCookie.from("access_token", "").maxAge(0).build();
        ResponseCookie deletedRefresh = ResponseCookie.from("refresh_token", "").maxAge(0).build();

        when(cookieService.extractAccessToken(any())).thenReturn("access.jwt.token");
        when(cookieService.extractRefreshToken(any())).thenReturn("refresh.jwt.token");
        when(cookieService.deleteAccessTokenCookie()).thenReturn(deletedAccess);
        when(cookieService.deleteRefreshTokenCookie()).thenReturn(deletedRefresh);
        doNothing().when(authService).logout(anyString(), anyString(), any());

        mockMvc.perform(post("/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @Order(7)
    @DisplayName("7. POST /auth/logout - 401 when unauthenticated")
    void testLogout_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("8. PUT /auth/change-password - 204 No Content on success")
    @WithMockUser
    void testChangePassword_Valid_Returns204() throws Exception {
        ChangePassword request = new ChangePassword(
                "user@agrowerk.tech", "Auth@1234", "NewAuth@5678", "NewAuth@5678");

        ResponseCookie deletedAccess  = ResponseCookie.from("access_token", "").maxAge(0).build();
        ResponseCookie deletedRefresh = ResponseCookie.from("refresh_token", "").maxAge(0).build();

        doNothing().when(authService).changePassword(any());
        when(cookieService.deleteAccessTokenCookie()).thenReturn(deletedAccess);
        when(cookieService.deleteRefreshTokenCookie()).thenReturn(deletedRefresh);

        mockMvc.perform(put("/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @Order(9)
    @DisplayName("9. PUT /auth/change-password - 401 when unauthenticated")
    void testChangePassword_Unauthenticated_Returns401() throws Exception {
        ChangePassword request = new ChangePassword(
                "user@agrowerk.tech", "Auth@1234", "NewAuth@5678", "NewAuth@5678");

        mockMvc.perform(put("/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(10)
    @DisplayName("10. PUT /auth/change-password - 401 when wrong current password")
    @WithMockUser
    void testChangePassword_WrongCurrentPassword_Returns401() throws Exception {
        ChangePassword request = new ChangePassword(
                "user@agrowerk.tech", "Wrong@1234Password", "NewAuth@5678", "NewAuth@5678");

        doThrow(new BadCredentialsException("Current password is incorrect"))
                .when(authService).changePassword(any());

        mockMvc.perform(put("/auth/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(11)
    @DisplayName("11. GET /auth/me - 200 OK with user info when authenticated")
    @WithMockUser(username = "user@agrowerk.tech")
    void testGetCurrentUser_Authenticated_Returns200() throws Exception {
        UserInfoDto userInfo = new UserInfoDto(USER_ID,"Test User", "user@agrowerk.tech");

        when(authService.getCurrentUserInfo()).thenReturn(userInfo);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("user@agrowerk.tech"));
    }

    @Test
    @Order(12)
    @DisplayName("12. GET /auth/me - 401 when unauthenticated")
    void testGetCurrentUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}