package tech.agrowerk.business.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.auth.ChangePassword;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResponse;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.business.mapper.UserMapper;
import tech.agrowerk.infrastructure.security.JwtService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.BadCredentialsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.exception.local.InvalidPasswordException;
import tech.agrowerk.infrastructure.exception.local.InvalidTokenException;
import tech.agrowerk.infrastructure.model.User;
import tech.agrowerk.infrastructure.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder, AuthUtil authUtil, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authUtil = authUtil;
        this.userMapper = userMapper;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        String accessToken = jwtService.generateTokenFromUser(user);
        String refreshToken = jwtService.generateRefreshTokenFromUser(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    public void logout(String token) {
        jwtService.invalidateToken(token);
    }

    public LoginResponse refreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtService.decodeToken(refreshToken);

            String tokenType = jwt.getClaim("type");
            if (!"refresh".equals(tokenType)) {
                throw new InvalidTokenException("Invalid token type");
            }

            String jti = jwt.getClaimAsString("jti");
            if (jwtService.isTokenBlacklisted(jti)) {
                throw new InvalidTokenException("Token has been revoked");
            }

            String username = jwt.getSubject();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            String newAccessToken = jwtService.generateTokenFromUser(user);
            String newRefreshToken = jwtService.generateRefreshTokenFromUser(user);

            return new LoginResponse(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            throw new InvalidTokenException("Invalid refresh token");
        }
    }

    public void changePassword(ChangePassword changePassword) {
        User user = userRepository.findByEmail(changePassword.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (passwordEncoder.matches(changePassword.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password must be different from current password.");
        }
        user.setPassword(passwordEncoder.encode(changePassword.newPassword()));
        userRepository.save(user);
    }

    public UserInfoDto getCurrentUserInfo() {
        AuthenticatedUser authUser = authUtil.getAuthenticatedUser();
        return userMapper.toUserInfoDto(authUser);
    }
}
