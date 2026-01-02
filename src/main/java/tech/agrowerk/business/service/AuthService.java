package tech.agrowerk.business.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.auth.LoginRequest;
import tech.agrowerk.application.dto.auth.LoginResponse;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, JwtEncoder jwtEncoder, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public LoginResponse login(LoginRequest loginRequest) {
        var user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        if (!user.isLoginCorrect(loginRequest, passwordEncoder)) {
            throw new BadCredentialsException();
        }

        var now = Instant.now();
        var expiresIn = 3600L;

        var claims = JwtClaimsSet.builder()
                .issuer("mybackend")
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .build();

        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return new LoginResponse(jwtValue);
    }

    public void changePassword(ChangePassword changePassword) {
        var user = userRepository.findUserByEmail(changePassword.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(changePassword.newPassword()));
        userRepository.save(user);
    }

    public UserInfoDto getInfo(Authentication authentication) {
        var jwt = (Jwt) authentication.getPrincipal();

        Long userId = Long.valueOf(jwt.getSubject());

        var currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));

        return new UserInfoDto(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getOrcid(),
                currentUser.getEmail()
        );
    }
}
