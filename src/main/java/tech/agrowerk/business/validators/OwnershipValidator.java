package tech.agrowerk.business.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.model.property.UserProperty;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;

import java.util.UUID;

@Component
@Slf4j
public class OwnershipValidator {

    private final UserPropertyRepository userPropertyRepository;

    public OwnershipValidator(UserPropertyRepository userPropertyRepository) {
        this.userPropertyRepository = userPropertyRepository;
    }

    public void validateOwnership(UUID propertyId, UUID userId) {
        if (!userPropertyRepository.existsByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId)) {
            throw new AccessDeniedException("You don't have access to this property");
        }
    }

    public void validateEditPermission(UUID propertyId, UUID userId) {
        UserProperty link = userPropertyRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access"));

        if (!link.isMasterOwner() && !link.isCanEdit()) {
            throw new AccessDeniedException("You don't have permission to edit this property");
        }
    }

    public void validateMasterOwnership(UUID propertyId, UUID userId) {
        UserProperty link = userPropertyRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access"));

        if (!link.isMasterOwner()) {
            throw new AccessDeniedException("Only the master owner can perform this action");
        }
    }
}