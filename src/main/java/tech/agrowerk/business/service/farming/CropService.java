package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.farming.CreateCropRequest;
import tech.agrowerk.application.dto.request.farming.UpdateCropRequest;
import tech.agrowerk.application.dto.response.farming.CropResponse;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.business.mapper.farming.CropMapper;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.farming.CropRepository;
import tech.agrowerk.infrastructure.repository.file.FileMetadataRepository;

import java.util.UUID;

@Service
@Slf4j
public class CropService {
    private final CropRepository cropRepository;
    private final UserRepository userRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;
    private final CropMapper cropMapper;
    private final AuthUtil authUtil;

    public CropService(CropRepository cropRepository,
                       UserRepository userRepository,
                       FileMetadataRepository fileMetadataRepository,
                       FileStorageService fileStorageService,
                       CropMapper cropMapper,
                       AuthUtil authUtil) {
        this.cropRepository = cropRepository;
        this.userRepository = userRepository;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileStorageService = fileStorageService;
        this.cropMapper = cropMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public CropResponse createCrop(CreateCropRequest request) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        if (cropRepository.existsByNameIgnoreCase(request.name())) {
            throw new EntityAlreadyExistsException("Crop already exists");
        }

        Crop crop = cropMapper.toEntity(request);

        User user = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        crop.setCreatedBy(user);

        Crop saved = cropRepository.save(crop);

        log.info("Crop created id={}", saved.getId());
        return cropMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<CropResponse> listCrops(Pageable pageable) {
        return cropRepository.findAll(pageable)
                .map(cropMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CropResponse> searchByName(String name, Pageable pageable) {
       return cropRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(cropMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CropResponse findById(UUID id) {
        return cropRepository.findById(id)
                .map(cropMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));
    }

    @Transactional
    public CropResponse updateCrop(UUID cropId, UpdateCropRequest request) {
        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        if (!crop.getCreatedBy().getId().equals(auth.id())) {
            throw new AccessDeniedException("Only the admin who created this crop can edit");
        }

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            crop.setName(request.name());
            hasChanges = true;
        }
        if (request.scientificName() != null && !request.scientificName().isBlank()) {
            crop.setScientificName(request.scientificName());
            hasChanges = true;
        }
        if (request.growthCycleDays() != null) {
            crop.setGrowthCycleDays(request.growthCycleDays());
            hasChanges = true;
        }
        if (request.cropCategory() != null) {
            crop.setCropCategory(request.cropCategory());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for crop id={}", cropId);
        }

        log.info("Crop updated id={}", cropId);
        return cropMapper.toResponse(crop);
    }

    @Transactional
    public FileUploadResponse uploadPhoto(UUID cropId, MultipartFile file) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        cropRepository.findById(cropId)
                .orElseThrow(() -> new EntityNotFoundException("Crop not found"));

        fileMetadataRepository.findByEntityIdAndFileCategoryAndDeletedFalse(
                cropId, FileCategory.CROP_PHOTO
        ).ifPresent(existing -> fileStorageService.delete(existing.getId()));

        log.info("Crop photo uploaded cropId={}", cropId);
        return fileStorageService.upload(file, FileCategory.CROP_PHOTO, cropId);
    }
}
