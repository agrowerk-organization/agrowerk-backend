package tech.agrowerk.infrastructure.security;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import tech.agrowerk.infrastructure.model.User;
import tech.agrowerk.infrastructure.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws EntityNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found"));

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().getName(),
                user.isDeleted()
        );
    }
}
