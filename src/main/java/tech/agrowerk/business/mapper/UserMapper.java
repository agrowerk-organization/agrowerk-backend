package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreateUserRequest;
import tech.agrowerk.application.dto.response.UserResponse;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.infrastructure.model.core.Role;
import tech.agrowerk.infrastructure.model.core.User;

@Component
public class UserMapper {
    public User toEntity(CreateUserRequest dto, Role role) {
        User user = new User();

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setCpf(dto.cpf());
        user.setTelephone(dto.telephone());
        user.setRole(role);

        return user;
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTelephone(),
                user.getRole() != null ? user.getRole().getName().name() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public UserInfoDto toUserInfoDto(User user) {
        return new UserInfoDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
