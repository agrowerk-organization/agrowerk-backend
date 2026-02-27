package tech.agrowerk.business.service.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

@Service
@Slf4j
public class AuthHelperService {

    private final UserRepository userRepository;

    public AuthHelperService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedAttempt(User user) {
        user.incrementFailedLoginAttempts();
        userRepository.save(user);
    }
}
