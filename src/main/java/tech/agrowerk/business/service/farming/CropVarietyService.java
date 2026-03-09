package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.create.CreateCropVarietyRequest;
import tech.agrowerk.application.dto.request.update.UpdateCropVarietyRequest;
import tech.agrowerk.application.dto.response.CropVarietyResponse;
import tech.agrowerk.business.mapper.CropVarietyMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.farming.CropVariety;
import tech.agrowerk.infrastructure.model.farming.enums.BrazilRegion;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;
import tech.agrowerk.infrastructure.repository.farming.CropVarietyRepository;

import java.util.UUID;

@Service
@Slf4j
public class CropVarietyService {

    private final CropVarietyRepository cropVarietyRepository;
    private final CropRepository cropRepository;
    private final CropVarietyMapper cropVarietyMapper;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    public CropVarietyService(CropVarietyRepository cropVarietyRepository,
                              CropRepository cropRepository,
                              CropVarietyMapper cropVarietyMapper,
                              UserRepository userRepository,
                              AuthUtil authUtil) {
        this.cropVarietyRepository = cropVarietyRepository;
        this.cropRepository = cropRepository;
        this.cropVarietyMapper = cropVarietyMapper;
        this.userRepository = userRepository;
        this.authUtil = authUtil;
    }

    @Transactional
    public CropVarietyResponse createVariety(CreateCropVarietyRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Crop crop = cropRepository.findById(request.cropId())
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));

        if (cropVarietyRepository.existsByNameIgnoreCaseAndCrop_Id(
                request.name(), request.cropId())) {
            throw new EntityAlreadyExistsException("Variety already exists for this crop");
        }

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        CropVariety variety = cropVarietyMapper.toEntity(request, crop, user);
        CropVariety saved = cropVarietyRepository.save(variety);

        log.info("CropVariety created id={} crop={}", saved.getId(), request.cropId());
        return cropVarietyMapper.toResponse(saved);
    }

    @Cacheable(value = "cropVarieties", key = "#cropId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public Page<CropVarietyResponse> findByCrop(UUID cropId, Pageable pageable) {
        return cropVarietyRepository.findByCrop_Id(cropId, pageable)
                .map(cropVarietyMapper::toResponse);
    }

    @Cacheable(value = "cropVarieties", key = "#cropId + ':' + #name",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public Page<CropVarietyResponse> searchByName(
            UUID cropId, String name, Pageable pageable) {
        return cropVarietyRepository
                .findByCrop_IdAndNameContainingIgnoreCase(cropId, name, pageable)
                .map(cropVarietyMapper::toResponse);
    }

    public CropVarietyResponse updateCropVariety(UUID cropVarietyId, UpdateCropVarietyRequest request) {
        CropVariety cropVariety = cropVarietyRepository.findById(cropVarietyId)
                .orElseThrow(() -> new EntityNotFoundException("Crop variety not found"));

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        if (!cropVariety.getCreatedBy().getId().equals(auth.id())) {
            throw new AccessDeniedException("Only producer who created this crop variety can edit");
        }

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            cropVariety.setName(request.name());
            hasChanges = true;
        }

         if (request.description() != null && !request.description().isBlank()) {
             cropVariety.setDescription(request.description());
             hasChanges = true;
         }

         if (request.region() != null && !request.region().isBlank()) {
             cropVariety.setRegion(BrazilRegion.valueOf(request.region()));
             hasChanges = true;
         }

        if (!hasChanges) {
            log.warn("No changes for crop id={}", cropVarietyId);
        }

        log.info("Crop updated id={}", cropVarietyId);
        return cropVarietyMapper.toResponse(cropVariety);
    }
}
