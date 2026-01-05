package tech.agrowerk.business.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tech.agrowerk.application.dto.crud.create.CreateUserRequest;
import tech.agrowerk.application.dto.crud.get.UserResponse;
import tech.agrowerk.business.mapper.UserMapper;
import tech.agrowerk.infrastructure.enums.RoleType;
import tech.agrowerk.infrastructure.model.Role;
import tech.agrowerk.infrastructure.model.User;
import tech.agrowerk.infrastructure.repository.RoleRepository;
import tech.agrowerk.infrastructure.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private Role role;
    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest(
                "José Pereira",
                "jose@gmail.com",
                "senha900",
                "senha900",
                "(85) 99999-9999",
                "00950833320",
                2L
        );

        role = new Role();
        role.setId(2L);
        role.setName(RoleType.PRODUCER);

        user = new User();
        user.setId(2L);
        user.setName("José Pereira");
        user.setEmail("jose@gmail.com");
        user.setPassword("senha900");
        user.setTelephone("(85) 99999-9999");
        user.setCpf("00950833320");
        user.setRole(role);

        userResponse = new UserResponse(
                2L,
                "José Pereira",
                "jose@gmail.com",
                "(85) 99999-9999",
                "PRODUCER",
                LocalDateTime.now(),
                null
        );
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userMapper.toEntity(createUserRequest, role)).thenReturn(user);
        when(passwordEncoder.encode("senha900")).thenReturn("$2a$10$encodedPasswordHashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(createUserRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("José Pereira");
        assertThat(result.email()).isEqualTo("jose@gmail.com");
        assertThat(result.telephone()).isEqualTo("(85) 99999-9999");
        assertThat(result.role()).isEqualTo("PRODUCER");

        verify(roleRepository, times(1)).findById(2L);
        verify(passwordEncoder, times(1)).encode("senha900");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toResponse(user);
    }

    @Test
    @DisplayName("Deve lançar exceção quando role não existir")
    void shouldThrowExceptionWhenRoleNotFound() {
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Role not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve encontrar usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findUserById(2L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.email()).isEqualTo("jose@gmail.com");

        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado por ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Deve encontrar usuário por email com sucesso")
    void shouldFindUserByEmailSuccessfully() {
        String email = "jose@gmail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.findUserByEmail(email);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(email);

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado por email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        String email = "naoexiste@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail(email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Deve criptografar senha ao criar usuário")
    void shouldEncryptPasswordWhenCreatingUser() {
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userMapper.toEntity(createUserRequest, role)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPasswordHashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        userService.createUser(createUserRequest);

        verify(passwordEncoder, times(1)).encode("senha900");
    }
}