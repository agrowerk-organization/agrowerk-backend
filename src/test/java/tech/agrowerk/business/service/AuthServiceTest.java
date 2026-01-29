package tech.agrowerk.business.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.service.auth.AuthService;
import tech.agrowerk.business.service.security.JwtService;
import tech.agrowerk.infrastructure.exception.local.BadCredentialsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.InvalidPasswordException;
import tech.agrowerk.infrastructure.exception.local.InvalidTokenException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("João Silva");
        user.setEmail("joao@email.com");
        user.setPassword("$2a$10$encodedPassword");

        loginRequest = new LoginRequest("joao@email.com", "senha123");
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtService.generateTokenFromUser(user)).thenReturn("access_token_123");
        when(jwtService.generateRefreshTokenFromUser(user)).thenReturn("refresh_token_123");

        AuthResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access_token_123");
        assertThat(result.refreshToken()).isEqualTo("refresh_token_123");

        verify(userRepository, times(1)).findByEmail("joao@email.com");
        verify(passwordEncoder, times(1)).matches("senha123", "$2a$10$encodedPassword");
        verify(jwtService, times(1)).generateTokenFromUser(user);
        verify(jwtService, times(1)).generateRefreshTokenFromUser(user);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não existir")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando senha estiver incorreta")
    void shouldThrowExceptionWhenPasswordIsIncorrect() {
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha123", "$2a$10$encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtService, never()).generateTokenFromUser(any());
    }

    @Test
    @DisplayName("Deve fazer logout invalidando o token")
    void shouldLogoutSuccessfully() {
        String token = "token_123";

        authService.logout(token);

        verify(jwtService, times(1)).invalidateToken(token);
    }

    @Test
    @DisplayName("Deve renovar token com sucesso")
    void shouldRefreshTokenSuccessfully() {
        String refreshToken = "refresh_token_123";
        Jwt jwt = mock(Jwt.class);

        when(jwtService.decodeToken(refreshToken)).thenReturn(jwt);
        when(jwt.getClaim("type")).thenReturn("refresh");
        when(jwt.getClaimAsString("jti")).thenReturn("jti_123");
        when(jwtService.isTokenBlacklisted("jti_123")).thenReturn(false);
        when(jwt.getSubject()).thenReturn("joao@email.com");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(jwtService.generateTokenFromUser(user)).thenReturn("new_access_token");
        when(jwtService.generateRefreshTokenFromUser(user)).thenReturn("new_refresh_token");

        AuthResponse result = authService.refreshToken(refreshToken);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("new_access_token");
        assertThat(result.refreshToken()).isEqualTo("new_refresh_token");
    }

    @Test
    @DisplayName("Deve lançar exceção quando refresh token for inválido")
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        String refreshToken = "invalid_token";
        when(jwtService.decodeToken(refreshToken)).thenThrow(new RuntimeException("Invalid token"));

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("Deve lançar exceção quando token estiver na blacklist")
    void shouldThrowExceptionWhenTokenIsBlacklisted() {
        String refreshToken = "refresh_token_123";
        Jwt jwt = mock(Jwt.class);

        when(jwtService.decodeToken(refreshToken)).thenReturn(jwt);
        when(jwt.getClaim("type")).thenReturn("refresh");
        when(jwt.getClaimAsString("jti")).thenReturn("jti_123");
        when(jwtService.isTokenBlacklisted("jti_123")).thenReturn(true);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    @DisplayName("Deve trocar senha com sucesso")
    void shouldChangePasswordSuccessfully() {
        ChangePassword changePassword = new ChangePassword("joao@email.com", "novaSenha123", "novaSenha123");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("novaSenha123", "$2a$10$encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("$2a$10$newEncodedPassword");

        authService.changePassword(changePassword);

        verify(userRepository, times(1)).findByEmail("joao@email.com");
        verify(passwordEncoder, times(1)).matches("novaSenha123", "$2a$10$encodedPassword");
        verify(passwordEncoder, times(1)).encode("novaSenha123");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção quando nova senha e confirmação forem diferentes")
    void shouldThrowExceptionWhenNewPasswordAndConfirmationDoNotMatch() {
        ChangePassword changePassword = new ChangePassword("joao@email.com", "senhaAtual", "outraSenhaAtual");
        when(userRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaAtual", "$2a$10$encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(changePassword))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("New password must be different from current password.");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha de recuperação for igual à que já existe no banco")
    void shouldThrowExceptionWhenRecoveredPasswordIsSameAsOld() {
        String email = "joao@email.com";
        String novaSenha = "senha_antiga_123";
        ChangePassword request = new ChangePassword(email, novaSenha, novaSenha);

        User userNoBanco = new User();
        userNoBanco.setPassword("$2a$10$hashDaSenhaAntiga");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userNoBanco));
        when(passwordEncoder.matches(novaSenha, "$2a$10$hashDaSenhaAntiga")).thenReturn(true);

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("New password must be different from current password.");
    }

    @Test
    @DisplayName("Deve retornar informações do usuário autenticado")
    void shouldGetUserInfoSuccessfully() {
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwtService.extractUserId(jwt)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserInfoDto result = authService.getInfo(authentication);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.email()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado ao buscar info")
    void shouldThrowExceptionWhenUserNotFoundInGetInfo() {
        Authentication authentication = mock(Authentication.class);
        Jwt jwt = mock(Jwt.class);

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwtService.extractUserId(jwt)).thenReturn(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getInfo(authentication))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }
}