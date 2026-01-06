package tech.agrowerk.business.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.crud.create.CreateUserRequest;
import tech.agrowerk.application.dto.crud.get.UserResponse;
import tech.agrowerk.business.mapper.UserMapper;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.model.Role;
import tech.agrowerk.infrastructure.model.User;
import tech.agrowerk.infrastructure.repository.RoleRepository;
import tech.agrowerk.infrastructure.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
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

        newUser.setLastLogin(LocalDateTime.now());

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
    public UserResponse findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return userMapper.toResponse(user);
    }
}
