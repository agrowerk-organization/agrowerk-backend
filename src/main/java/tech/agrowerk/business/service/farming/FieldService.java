package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.farming.CreateFieldRequest;
import tech.agrowerk.application.dto.request.farming.UpdateFieldRequest;
import tech.agrowerk.application.dto.response.farming.FieldResponse;
import tech.agrowerk.business.mapper.farming.FieldMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.farming.Field;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.valueobject.Geolocation;
import tech.agrowerk.infrastructure.repository.farming.FieldRepository;
import tech.agrowerk.infrastructure.repository.farming.PlantingRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FieldService {

    private final FieldRepository fieldRepository;
    private final PropertyRepository propertyRepository;
    private final PlantingRepository plantingRepository;
    private final FieldMapper fieldMapper;
    private final AuthUtil authUtil;
    private final OwnershipValidator ownershipValidator;

    private static final int MAX_ACTIVE_CROPS_PER_FIELD = 3;

    public FieldService(FieldRepository fieldRepository,
                        PropertyRepository propertyRepository,
                        UserPropertyRepository userPropertyRepository,
                        PlantingRepository plantingRepository,
                        FieldMapper fieldMapper,
                        AuthUtil authUtil,
                        OwnershipValidator ownershipValidator) {
        this.fieldRepository = fieldRepository;
        this.propertyRepository = propertyRepository;
        this.plantingRepository = plantingRepository;
        this.fieldMapper = fieldMapper;
        this.authUtil = authUtil;
        this.ownershipValidator = ownershipValidator;
    }

    @Transactional
    public FieldResponse createField(CreateFieldRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        ownershipValidator.validateOwnership(request.propertyId(), auth.id());

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (fieldRepository.existsByNameIgnoreCaseAndProperty_Id(request.name(), request.propertyId())) {
            throw new EntityAlreadyExistsException("Field name already exists in this property");
        }

        BigDecimal usedArea = fieldRepository.sumAreaByProperty(request.propertyId());
        BigDecimal availableArea = property.getTotalArea().subtract(usedArea);

        if (request.areaHectares().compareTo(availableArea) > 0) {
            throw new IllegalArgumentException(
                    String.format("Field area %.2fha exceeds available property area %.2fha",
                            request.areaHectares(), availableArea)
            );
        }

        validateFieldGeolocation(property, request);

        if (request.fieldStatus() != null &&
            request.fieldStatus() == FieldStatus.DEGRADED) {
            throw new IllegalArgumentException(
                    "Cannot create a field with DEGRADED status"
            );
        }

        Field field = fieldMapper.toEntity(request, property);
        Field saved = fieldRepository.save(field);

        return fieldMapper.toResponse(saved);
    }

    @Cacheable(value = "fields", key = "#propertyId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public Page<FieldResponse> findByProperty(UUID propertyId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        return fieldRepository.findByProperty_Id(propertyId, pageable)
                .map(fieldMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FieldResponse findById(UUID fieldId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new EntityNotFoundException("Field not found"));

        ownershipValidator.validateOwnership(field.getProperty().getId(), auth.id());
        return fieldMapper.toResponse(field);
    }

    @Transactional
    public FieldResponse updateField(UUID fieldId, UpdateFieldRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new EntityNotFoundException("Field not found"));

        ownershipValidator.validateEditPermission(field.getProperty().getId(), auth.id());

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            field.setName(request.name());
            hasChanges = true;
        }
        if (request.description() != null) {
            field.setDescription(request.description());
            hasChanges = true;
        }
        if (request.soilType() != null) {
            field.setSoilType(request.soilType());
            hasChanges = true;
        }
        if (request.notes() != null) {
            field.setNotes(request.notes());
            hasChanges = true;
        }
        if (request.fieldStatus() != null) {
            validateStatusTransition(field.getFieldStatus(), request.fieldStatus());

            if (request.fieldStatus() == FieldStatus.ACTIVE) {
                boolean hasActivePlanting = plantingRepository
                        .existsByField_IdAndPlantingStatus(
                                fieldId, PlantingStatus.IN_PROGRESS);
                if (hasActivePlanting) {
                    throw new IllegalArgumentException(
                            "Cannot reactivate field with active plantings"
                    );
                }
            }

            field.setFieldStatus(request.fieldStatus());
            hasChanges = true;
        }

        if (request.latitude() != null && request.longitude() != null) {
            field.setGeolocation(new Geolocation(request.latitude(), request.longitude()));
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for field id={}", fieldId);
        }

        log.info("Field updated id={}", fieldId);
        return fieldMapper.toResponse(field);
    }

    public void validateFieldAvailability(UUID fieldId) {
        long activeCrops = fieldRepository.countActiveCropsInField(fieldId);
        if (activeCrops >= MAX_ACTIVE_CROPS_PER_FIELD) {
            throw new IllegalArgumentException(
                    "Field already has " + MAX_ACTIVE_CROPS_PER_FIELD + " active crops — maximum reached"
            );
        }
    }

    private void validateFieldGeolocation(Property property, CreateFieldRequest request) {
        if (request.latitude() == null || request.longitude() == null) return;
        if (property.getLatitude() == null || property.getLongitude() == null) return;

        BigDecimal tolerance = new BigDecimal("0.05");

        boolean latValid = request.latitude()
                .subtract(property.getLatitude()).abs()
                .compareTo(tolerance) <= 0;

        boolean lonValid = request.longitude()
                .subtract(property.getLongitude()).abs()
                .compareTo(tolerance) <= 0;

        if (!latValid || !lonValid) {
            throw new IllegalArgumentException(
                    "Field geolocation is too far from property location"
            );
        }
    }

    private void validateStatusTransition(FieldStatus current, FieldStatus next) {
        Map<FieldStatus, Set<FieldStatus>> allowed = Map.of(
                FieldStatus.ACTIVE,      Set.of(FieldStatus.INACTIVE, FieldStatus.DEGRADED),
                FieldStatus.INACTIVE,    Set.of(FieldStatus.RESTING, FieldStatus.DEGRADED),
                FieldStatus.RESTING,     Set.of(FieldStatus.MAINTENANCE, FieldStatus.DEGRADED),
                FieldStatus.MAINTENANCE, Set.of(FieldStatus.ACTIVE, FieldStatus.DEGRADED),
                FieldStatus.DEGRADED,    Set.of()
        );

        if (!allowed.get(current).contains(next)) {
            throw new IllegalArgumentException(
                    String.format("Invalid field status transition: %s → %s", current, next)
            );
        }
    }
}
