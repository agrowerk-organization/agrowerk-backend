package tech.agrowerk.business.mapper.core;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.core.CreateUserRequest;
import tech.agrowerk.application.dto.response.core.UserProfileResponse;
import tech.agrowerk.application.dto.response.core.UserResponse;
import tech.agrowerk.application.dto.user.UserInfoDto;
import tech.agrowerk.infrastructure.model.core.Role;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.file.FileMetadata;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;
import tech.agrowerk.infrastructure.repository.file.FileMetadataRepository;

@Component
public class UserMapper {

    private final AddressMapper addressMapper;

    public UserMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }


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
                user.getEmail(),
                user.getRole().getName().toString()
        );
    }

    public UserProfileResponse toUserProfileResponse(User user, String avatarUrl) {

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTelephone(),
                maskCpf(user.getCpf()),
                addressMapper.toAddressResponse(user.getAddress()),
                user.isEmailVerified(),
                user.isPhoneVerified(),
                user.isMfaEnabled(),
                user.getLastLogin(),
                user.getLastPasswordChange(),
                user.isRequirePasswordChange(),
                user.isTermsAccepted(),
                user.isPrivacyPolicyAccepted(),
                user.isMarketingConsent(),
                user.getCreatedAt(),
                avatarUrl
        );
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 11) return null;

        return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
    }
}
