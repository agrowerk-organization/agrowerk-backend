package tech.agrowerk.infrastructure.security;

import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.model.User;
import tech.agrowerk.infrastructure.repository.UserRepository;

@Service
public class JwtUserValidator {

    private final UserRepository userRepository;

    public JwtUserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User validate(Long userId, Integer tokenVersion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        if (user.isDeleted()) {
            throw new DisabledException("User deleted");
        }

        if (tokenVersion == null || tokenVersion != user.getTokenVersion()) {
            throw new AccessDeniedException("Token revoked");
        }

        return user;
    }
}
