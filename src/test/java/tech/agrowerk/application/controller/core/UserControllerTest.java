package tech.agrowerk.application.controller.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tech.agrowerk.application.controller.base.BaseControllerTest;
import tech.agrowerk.application.dto.crud.create.CreateUserRequest;
import tech.agrowerk.application.dto.crud.get.UserResponse;
import tech.agrowerk.application.dto.crud.update.UpdateUserRequest;
import tech.agrowerk.business.service.core.UserService;
import tech.agrowerk.infrastructure.config.TestSecurityConfig;
import tech.agrowerk.infrastructure.exception.global.AdvancedGlobalExceptionHandler;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({TestSecurityConfig.class, AdvancedGlobalExceptionHandler.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String USER_EMAIL = "user@agrowerk.tech";

    @Override
    protected void setUp() {
        super.setUpSecurity();
    }

    private UserResponse buildUserResponse() {
        return new UserResponse(USER_ID, "Test User", USER_EMAIL, "(88) 99999-0000", "PRODUCER", Instant.now(), null);
    }

    private CreateUserRequest buildCreateRequest() {
        return new CreateUserRequest(
                "Test User",
                USER_EMAIL,
                "Auth@1234",
                "Auth@1234",
                "(88) 99999-0000",
                "529.982.247-25",
                UUID.randomUUID()
        );
    }

    @Test
    @Order(1)
    @DisplayName("1. POST /users/register - 200 OK with created user")
    void testRegister_ValidRequest_Returns200() throws Exception {
        when(userService.createUser(any())).thenReturn(buildUserResponse());

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @Order(2)
    @DisplayName("2. POST /users/register - 409 when email already exists")
    void testRegister_DuplicateEmail_Returns409() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new EntityAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCreateRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("3. POST /users/register - 400 when body has validation errors")
    void testRegister_InvalidBody_Returns400() throws Exception {
        String invalidBody = """
                {
                  "name": "",
                  "email": "not-an-email",
                  "password": "weak",
                  "confirmPassword": "weak",
                  "roleId": "%s"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Order(4)
    @DisplayName("4. GET /users/get-user-by-id/{id} - 200 OK for SUPPLIER_ADMIN")
    @WithMockUser(authorities = "SUPPLIER_ADMIN")
    void testGetUserById_AsAdmin_Returns200() throws Exception {
        when(userService.findUserById(USER_ID)).thenReturn(buildUserResponse());

        mockMvc.perform(get("/users/get-user-by-id/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()));
    }

    @Test
    @Order(5)
    @DisplayName("5. GET /users/get-user-by-id/{id} - 401 when unauthenticated")
    void testGetUserById_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/users/get-user-by-id/{id}", USER_ID))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @Order(6)
    @DisplayName("6. GET /users/get-user-by-email/{email} - 200 OK for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testGetUserByEmail_AsSystemAdmin_Returns200() throws Exception {
        when(userService.findUserByEmail(USER_EMAIL)).thenReturn(buildUserResponse());

        mockMvc.perform(get("/users/get-user-by-email/{email}", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /users/get-user-by-email/{email} - 403 for non-admin authenticated user")
    @WithMockUser(authorities = "PRODUCER")
    void testGetUserByEmail_AsProducer_Returns403() throws Exception {
        mockMvc.perform(get("/users/get-user-by-email/{email}", USER_EMAIL))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(8)
    @DisplayName("8. GET /users/get/me - 200 OK when authenticated")
    @WithMockUser
    void testGetCurrentUser_Authenticated_Returns200() throws Exception {
        when(userService.getCurrentUser()).thenReturn(buildUserResponse());

        mockMvc.perform(get("/users/get/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));
    }

    @Test
    @Order(9)
    @DisplayName("9. GET /users/get/me - 401 when unauthenticated")
    void testGetCurrentUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/users/get/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(10)
    @DisplayName("10. GET /users/list-users - 200 OK with paginated result for SYSTEM_ADMIN")
    @WithMockUser(authorities = "SYSTEM_ADMIN")
    void testListUsers_AsSystemAdmin_Returns200() throws Exception {
        Page<UserResponse> page = new PageImpl<>(
                List.of(buildUserResponse()),
                PageRequest.of(0, 10),
                1
        );
        when(userService.listUsers(any())).thenReturn(page);

        mockMvc.perform(get("/users/list-users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value(USER_EMAIL))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Order(11)
    @DisplayName("11. GET /users/list-users - 403 for non-admin authenticated user")
    @WithMockUser(authorities = "PRODUCER")
    void testListUsers_AsProducer_Returns403() throws Exception {
        mockMvc.perform(get("/users/list-users"))
                .andExpect(status().isForbidden());
    }


    @Test
    @Order(12)
    @DisplayName("12. PUT /users/update/me - 200 OK when authenticated")
    @WithMockUser
    void testUpdateUser_Authenticated_Returns200() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name",  null,"(88) 88888-8888", null);

        when(userService.updateUser(any())).thenReturn(buildUserResponse());

        mockMvc.perform(put("/users/update/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    @DisplayName("13. PUT /users/update/me - 401 when unauthenticated")
    void testUpdateUser_Unauthenticated_Returns401() throws Exception {
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Updated Name", null, null, null);

        mockMvc.perform(put("/users/update/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(14)
    @DisplayName("14. DELETE /users/delete/me - 204 No Content when authenticated")
    @WithMockUser
    void testDeleteUser_Authenticated_Returns204() throws Exception {
        doNothing().when(userService).deleteUserById();

        mockMvc.perform(delete("/users/delete/me")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(15)
    @DisplayName("15. DELETE /users/delete/me - 401 when unauthenticated")
    void testDeleteUser_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(delete("/users/delete/me")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}