package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.farming.CreateCropVarietyRequest;
import tech.agrowerk.application.dto.request.farming.UpdateCropVarietyRequest;
import tech.agrowerk.application.dto.response.farming.CropVarietyResponse;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.business.mapper.farming.CropVarietyMapper;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.farming.CropVariety;
import tech.agrowerk.infrastructure.model.farming.enums.BrazilRegion;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;
import tech.agrowerk.infrastructure.repository.farming.CropVarietyRepository;
import tech.agrowerk.infrastructure.repository.file.FileMetadataRepository;

import java.util.UUID;

@Service
@Slf4j
public class CropVarietyService {

    private final CropVarietyRepository cropVarietyRepository;
    private final CropRepository cropRepository;
    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final CropVarietyMapper cropVarietyMapper;
    private final AuthUtil authUtil;

    public CropVarietyService(CropVarietyRepository cropVarietyRepository,
                              CropRepository cropRepository,
                              FileMetadataRepository fileMetadataRepository,
                              FileStorageService fileStorageService,
                              CropVarietyMapper cropVarietyMapper,
                              UserRepository userRepository,
                              AuthUtil authUtil) {
        this.cropVarietyRepository = cropVarietyRepository;
        this.cropRepository = cropRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStorageService = fileStorageService;
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

    @Transactional(readOnly = true)
    public Page<CropVarietyResponse> findByCrop(UUID cropId, Pageable pageable) {
        return cropVarietyRepository.findByCrop_Id(cropId, pageable)
                .map(cropVarietyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CropVarietyResponse> searchByName(UUID cropId, String name, Pageable pageable) {
        return cropVarietyRepository
                .findByCrop_IdAndNameContainingIgnoreCase(cropId, name, pageable)
                .map(cropVarietyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CropVarietyResponse findById(UUID cropVarietyId) {
        CropVariety cropVariety = cropVarietyRepository.findById(cropVarietyId)
                .orElseThrow(() -> new EntityNotFoundException("Crop variety not found"));

        return cropVarietyMapper.toResponse(cropVariety);
    }

    @Transactional
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

    @Transactional
    public FileUploadResponse uploadPhoto(UUID cropId, MultipartFile file) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        cropRepository.findById(cropId)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));

        fileMetadataRepository.findByEntityIdAndFileCategoryAndDeletedFalse(
                cropId, FileCategory.CROP_VARIETY_PHOTO
        ).ifPresent(existing -> fileStorageService.delete(existing.getId()));

        log.info("Crop photo uploaded cropId={}", cropId);
        return fileStorageService.upload(file, FileCategory.CROP_VARIETY_PHOTO, cropId);
    }
}
