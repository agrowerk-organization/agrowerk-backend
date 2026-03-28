package tech.agrowerk.business.service.property;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.property.AddFarmUnitRequest;
import tech.agrowerk.application.dto.request.property.AddOwnerRequest;
import tech.agrowerk.application.dto.request.property.CreatePropertyRequest;
import tech.agrowerk.application.dto.request.property.UpdateFarmUnitRequest;
import tech.agrowerk.application.dto.request.property.UpdatePropertyRequest;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.application.dto.response.property.PropertyResponse;
import tech.agrowerk.business.mapper.property.PropertyMapper;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.property.State;
import tech.agrowerk.infrastructure.model.property.UserProperty;
import tech.agrowerk.infrastructure.model.property.enums.OwnerRemovalReason;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.StateRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final StateRepository stateRepository;
    private final FileStorageService fileStorageService;
    private final AuthUtil authUtil;
    private final PropertyMapper propertyMapper;
    private final OwnershipValidator ownershipValidator;

    public PropertyService(PropertyRepository propertyRepository,
                           UserRepository userRepository,
                           UserPropertyRepository userPropertyRepository,
                           StateRepository stateRepository,
                           FileStorageService fileStorageService,
                           AuthUtil authUtil,
                           PropertyMapper propertyMapper,
                           OwnershipValidator ownershipValidator) {
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.stateRepository = stateRepository;
        this.fileStorageService = fileStorageService;
        this.authUtil = authUtil;
        this.propertyMapper = propertyMapper;
        this.ownershipValidator = ownershipValidator;
    }

    @Transactional
    public PropertyResponse createProperty(CreatePropertyRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        State state = stateRepository.findById(request.stateId())
                .orElseThrow(() -> new EntityNotFoundException("State not found"));

        if (propertyRepository.existsByStateRegistration(request.stateRegistration())) {
            throw new EntityAlreadyExistsException("Property already registered");
        }

        validateFarmUnitsArea(request.totalArea(), request.units());

        Property property = propertyMapper.toEntity(request, state);

        Property saved = propertyRepository.save(property);

        UserProperty link = new UserProperty();
        link.setUser(user);
        link.setProperty(saved);
        link.setMasterOwner(true);
        userPropertyRepository.save(link);

        return propertyMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PropertyResponse findPropertyById(UUID propertyId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateMasterOwnership(propertyId, auth.id());
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
        return propertyMapper.toResponse(property);
    }

    @Transactional(readOnly = true)
    public Page<PropertyResponse> findMyProperties(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        return propertyRepository
                .findByUserLinksUserIdAndUserLinksIsActiveTrue(auth.id(), pageable)
                .map(propertyMapper::toResponse);

    }

    @Transactional
    public PropertyResponse updateProperty(UUID propertyId, UpdatePropertyRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            property.setName(request.name());
            hasChanges = true;
        }
        if (request.plantedArea() != null) {
            property.setPlantedArea(request.plantedArea());
            hasChanges = true;
        }
        if (request.totalArea() != null) {
            property.setTotalArea(request.totalArea());
            hasChanges = true;
        }
        if (request.mainCrop() != null && !request.mainCrop().isBlank()) {
            property.setMainCrop(request.mainCrop());
            hasChanges = true;
        }
        if (request.ruralRegistration() != null && !request.ruralRegistration().isBlank()) {
            property.setRuralRegistration(request.ruralRegistration());
            hasChanges = true;
        }
        if (request.address() != null) {
            property.setAddress(propertyMapper.toAddress(request.address()));
            hasChanges = true;
        }
        if (request.latitude() != null) {
            property.setLatitude(request.latitude());
            hasChanges = true;
        }
        if (request.longitude() != null) {
            property.setLongitude(request.longitude());
            hasChanges = true;
        }
        if (request.isActive() != null) {
            property.setIsActive(request.isActive());
            hasChanges = true;
        }

        if (request.totalArea() != null) {
            property.setTotalArea(request.totalArea());
            hasChanges = true;
        }

        if (request.units() != null) {
            validateFarmUnitsArea(property.getTotalArea(), request.units());

            property.getUnits().clear();
            property.getUnits().addAll(
                    request.units().stream()
                            .map(unitReq -> propertyMapper.toFarmUnitEntity(unitReq, property))
                            .toList()
            );
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for property id={}", propertyId);
        }

        log.info("Property updated id={}", propertyId);
        return propertyMapper.toResponse(property);
    }

    @Transactional
    public void addOwner(UUID propertyId, AddOwnerRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        User newOwner = userRepository.findById(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!newOwner.isProducer()) {
            throw new IllegalArgumentException("User is not a producer");
        }

        if (userPropertyRepository.existsByPropertyIdAndUserIdAndIsActiveTrue(propertyId, request.userId())) {
            throw new EntityAlreadyExistsException("User is already an owner");
        }

        UserProperty link = new UserProperty();
        link.setUser(newOwner);
        link.setProperty(property);
        link.setMasterOwner(false);
        link.setCanEdit(request.canEdit());
        userPropertyRepository.save(link);
    }

    @Transactional
    public void updateEditPermission(UUID propertyId, UUID targetUserId, boolean canEdit) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        UserProperty requesterLink = userPropertyRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, auth.id())
                .orElseThrow(() -> new AccessDeniedException("You don't have access"));

        if (!requesterLink.isMasterOwner()) {
            throw new AccessDeniedException("Only the master owner can grant permissions");
        }

        UserProperty targetLink = userPropertyRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

        if (targetLink.isMasterOwner()) {
            throw new IllegalArgumentException("Cannot change master owner permissions");
        }

        targetLink.setCanEdit(canEdit);
        targetLink.setCanEditGrantedAt(canEdit ? Instant.now() : null);
        targetLink.setCanEditGrantedBy(canEdit ? auth.id() : null);
    }

    @Transactional
    public void removeOwner(UUID propertyId, UUID targetUserId, OwnerRemovalReason reason) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        UserProperty requesterLink = userPropertyRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, auth.id())
                .orElseThrow(() -> new AccessDeniedException("You don't have access"));

        if (!requesterLink.isMasterOwner()) {
            throw new AccessDeniedException("Only the master owner can remove others");
        }

        if (auth.id().equals(targetUserId)) {
            throw new IllegalArgumentException("Master owner cannot remove themselves");
        }

        UserProperty targetLink = userPropertyRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

        targetLink.setActive(false);
        targetLink.setRemovedAt(Instant.now());
        targetLink.setRemovedBy(auth.id());
        targetLink.setRemovalReason(reason);
    }

    @Transactional
    public FileUploadResponse uploadPhoto(UUID propertyId, MultipartFile file) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateEditPermission(propertyId, auth.id());

        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        return fileStorageService.upload(file, FileCategory.PROPERTY_PHOTO, propertyId);
    }

    private void validateFarmUnitsArea(BigDecimal totalArea, List<?> units) {
        if (units == null || units.isEmpty() || totalArea == null) return;

        BigDecimal unitsTotalArea = BigDecimal.ZERO;

        for (Object unit : units) {
            BigDecimal area = BigDecimal.ZERO;
            if (unit instanceof AddFarmUnitRequest addRequest) {
                area = addRequest.area();
            } else if (unit instanceof UpdateFarmUnitRequest updateRequest) {
                area = updateRequest.area();
            }

            if (area != null) {
                unitsTotalArea = unitsTotalArea.add(area);
            }
        }

        if (unitsTotalArea.compareTo(totalArea) > 0) {
            throw new IllegalArgumentException(
                    String.format("The sum of farm unit areas (%.2f) exceeds the property total area (%.2f)",
                            unitsTotalArea, totalArea)
            );
        }
    }
}
