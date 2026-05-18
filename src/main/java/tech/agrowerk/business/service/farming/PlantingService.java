package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.farming.CreatePlantingRequest;
import tech.agrowerk.application.dto.request.farming.UpdatePlantingRequest;
import tech.agrowerk.application.dto.response.farming.PlantingResponse;
import tech.agrowerk.business.mapper.farming.PlantingMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.farming.*;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SeasonStatus;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.repository.farming.*;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;
import tech.agrowerk.infrastructure.repository.property.UserPropertyRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class PlantingService {

    private final PlantingRepository plantingRepository;
    private final PlantingInputRepository plantingInputRepository;
    private final PropertyRepository propertyRepository;
    private final FieldRepository fieldRepository;
    private final SeasonRepository seasonRepository;
    private final CropVarietyRepository cropVarietyRepository;
    private final UserPropertyRepository userPropertyRepository;
    private final FieldService fieldService;
    private final PlantingMapper plantingMapper;
    private final AuthUtil authUtil;
    private final OwnershipValidator ownershipValidator;

    public PlantingService(PlantingRepository plantingRepository,
                           PlantingInputRepository plantingInputRepository,
                           PropertyRepository propertyRepository,
                           FieldRepository fieldRepository,
                           SeasonRepository seasonRepository,
                           CropVarietyRepository cropVarietyRepository,
                           UserPropertyRepository userPropertyRepository,
                           FieldService fieldService,
                           PlantingMapper plantingMapper,
                           AuthUtil authUtil,
                           OwnershipValidator ownershipValidator) {
        this.plantingRepository = plantingRepository;
        this.plantingInputRepository = plantingInputRepository;
        this.propertyRepository = propertyRepository;
        this.fieldRepository = fieldRepository;
        this.seasonRepository = seasonRepository;
        this.cropVarietyRepository = cropVarietyRepository;
        this.userPropertyRepository = userPropertyRepository;
        this.fieldService = fieldService;
        this.plantingMapper = plantingMapper;
        this.authUtil = authUtil;
        this.ownershipValidator = ownershipValidator;
    }

    @Transactional
    public PlantingResponse createPlanting(CreatePlantingRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        ownershipValidator.validateOwnership(request.propertyId(), auth.id());

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        Field field = fieldRepository.findById(request.fieldId())
                .orElseThrow(() -> new EntityNotFoundException("Field not found"));

        if (!field.getProperty().getId().equals(request.propertyId())) {
            throw new IllegalArgumentException("Field does not belong to this property");
        }

        if (field.getFieldStatus() != FieldStatus.ACTIVE) {
            throw new IllegalArgumentException("Field is not available for planting");
        }

        Season season = seasonRepository.findById(request.seasonId())
                .orElseThrow(() -> new EntityNotFoundException("Season not found"));

        if (season.getSeasonStatus() != SeasonStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Season is not in progress");
        }

        if (!season.getProperty().getId().equals(request.propertyId())) {
            throw new IllegalArgumentException("Season does not belong to this property");
        }

        CropVariety cropVariety = cropVarietyRepository.findById(request.cropVarietyId())
                .orElseThrow(() -> new EntityNotFoundException("Crop variety not found"));

        Crop crop = cropVariety.getCrop();

        fieldService.validateFieldAvailability(request.fieldId());

        BigDecimal usedArea = plantingRepository
                .sumActivePlantingAreaByField(request.fieldId());
        BigDecimal availableArea = field.getAreaHectares().subtract(usedArea);

        if (request.areaHectares().compareTo(availableArea) > 0) {
            throw new IllegalArgumentException(
                    String.format("Planting area %.2fha exceeds available field area %.2fha",
                            request.areaHectares(), availableArea)
            );
        }

        if (request.plantingDate().isAfter(request.expectedHarvestDate())) {
            throw new IllegalArgumentException("Planting date must be before expected harvest date");
        }

        Planting planting = plantingMapper.toEntity(
                request, property, field, season, crop, cropVariety);

        Planting saved = plantingRepository.save(planting);

        field.setFieldStatus(FieldStatus.PLANTED);

        log.info("Planting created id={} field={} season={}",
                saved.getId(), request.fieldId(), request.seasonId());

        return plantingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PlantingResponse> findByProperty(UUID propertyId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        return plantingRepository.findByProperty_Id(propertyId, pageable)
                .map(plantingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PlantingResponse> findByField(UUID fieldId, Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new EntityNotFoundException("Field not found"));

        ownershipValidator.validateMasterOwnership(field.getProperty().getId(), auth.id());

        return plantingRepository.findByField_Id(fieldId, pageable)
                .map(plantingMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PlantingResponse findById(UUID plantingId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());
        return plantingMapper.toResponse(planting);
    }

    @Transactional
    public PlantingResponse updatePlanting(UUID plantingId, UpdatePlantingRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());

        boolean hasChanges = false;

        if (request.cropVarietyId() != null) {
            if (plantingInputRepository.existsByPlanting_Id(plantingId)) {
                throw new IllegalArgumentException(
                        "Crop variety cannot be changed after inputs have been registered"
                );
            }
            CropVariety cropVariety = cropVarietyRepository.findById(request.cropVarietyId())
                    .orElseThrow(() -> new EntityNotFoundException("Crop variety not found"));
            planting.setCropVariety(cropVariety);
            hasChanges = true;
        }

        if (request.plantingDate() != null) {
            if (plantingInputRepository.existsByPlanting_Id(plantingId)) {
                throw new IllegalArgumentException(
                        "Planting date cannot be changed after inputs have been registered"
                );
            }
            planting.setPlantingDate(request.plantingDate());
            hasChanges = true;
        }

         if (request.expectedHarvestDate() != null) {

             planting.setExpectedHarvestDate(request.expectedHarvestDate());
             hasChanges = true;
         }

        if (!hasChanges) {
            log.warn("No changes for planting id={}", plantingId);
        }

        log.info("Planting updated id={}", plantingId);
        return plantingMapper.toResponse(planting);
    }

    @Transactional
    public PlantingResponse cancelPlanting(UUID plantingId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), auth.id());

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only IN PROGRESS plantings can be cancelled");
        }

        planting.setPlantingStatus(PlantingStatus.CANCELLED);

        boolean hasOtherActivePlantings = plantingRepository
                .existsByField_IdAndPlantingStatus(
                        planting.getField().getId(), PlantingStatus.IN_PROGRESS);

        if (!hasOtherActivePlantings) {
            planting.getField().setFieldStatus(FieldStatus.ACTIVE);
            log.info("Field id={} returned to ACTIVE after planting cancellation",
                    planting.getField().getId());
        }

        log.info("Planting cancelled id={}", plantingId);
        return plantingMapper.toResponse(planting);
    }

    public Planting findAndValidateForHarvest(UUID plantingId, UUID userId) {
        Planting planting = plantingRepository.findById(plantingId)
                .orElseThrow(() -> new EntityNotFoundException("Planting not found"));

        ownershipValidator.validateOwnership(planting.getProperty().getId(), userId);

        if (planting.getPlantingStatus() != PlantingStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Only IN PROGRESS plantings can be harvested");
        }

        return planting;
    }
}
