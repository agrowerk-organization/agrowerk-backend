package tech.agrowerk.business.service.core;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.crud.create.CreateUserRequest;
import tech.agrowerk.application.dto.crud.get.UserResponse;
import tech.agrowerk.application.dto.crud.update.UpdateUserRequest;
import tech.agrowerk.business.mapper.UserMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.model.core.Role;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.repository.core.RoleRepository;
import tech.agrowerk.infrastructure.repository.core.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, AuthUtil authUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.authUtil = authUtil;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        Role role = roleRepository.findById(request.roleId())
                .orElseThrow( () -> new EntityNotFoundException("Role not found"));

        Optional<User> user = userRepository.findByEmail(request.email());

        if (user.isPresent()) {
            throw new EntityAlreadyExistsException("User already exists");
        }

        User newUser = userMapper.toEntity(request, role);

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        newUser.setLastLogin(Instant.now());

        User savedUser = userRepository.save(newUser);

        return userMapper.toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        return page.map(userMapper::toResponse);
    }

    @Transactional
    public UserResponse updateUser(UpdateUserRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
            hasChanges = true;
        }

        if (request.telephone() != null && !request.telephone().isBlank()) {
            user.setTelephone(request.telephone());
            hasChanges = true;
        }

        if (request.email() != null && !request.email().isBlank()) {
            if (userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
                throw new EntityAlreadyExistsException("Email already exists");
            }
            user.setEmail(request.email());
            hasChanges = true;
        }

        if (request.cpf() != null && !request.cpf().isBlank()) {
            if (userRepository.existsByCpfAndIdNot(request.cpf(), user.getId())) {
                throw new EntityAlreadyExistsException("Cpf already exists");
            }
            user.setCpf(request.cpf());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for user id={}", auth.id());
        }

        log.info("User updated id={}", auth.id());
        return userMapper.toResponse(user);
    }

    @Transactional
    public void deleteUserById() {
        AuthenticatedUser authenticatedUser = authUtil.getAuthenticatedUser();

        User user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setDeleted(true);
        user.setName("deleted");
        user.setEmail("deleted_" + user.getId() + "@non.local");
        user.setPassword("deleted");
        user.setCpf(null);
        user.setTelephone(null);
    }
}
