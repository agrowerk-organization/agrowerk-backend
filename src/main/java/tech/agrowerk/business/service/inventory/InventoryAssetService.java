package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.request.inventory.CreateInventoryAssetRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateInventoryAssetRequest;
import tech.agrowerk.application.dto.response.file.FileUploadResponse;
import tech.agrowerk.application.dto.response.inventory.InventoryAssetResponse;
import tech.agrowerk.business.mapper.inventory.InventoryAssetMapper;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.AccessDeniedException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.file.FileMetadata;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.repository.core.UserRepository;
import tech.agrowerk.infrastructure.repository.file.FileMetadataRepository;
import tech.agrowerk.infrastructure.repository.inventory.InventoryAssetRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InventoryAssetService {

    private final InventoryAssetRepository assetRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final OwnershipValidator ownershipValidator;
    private final InventoryAssetMapper assetMapper;
    private final AuthUtil authUtil;

    public InventoryAssetService(InventoryAssetRepository assetRepository, PropertyRepository propertyRepository, UserRepository userRepository, FileStorageService fileStorageService, FileMetadataRepository fileMetadataRepository, OwnershipValidator ownershipValidator, InventoryAssetMapper assetMapper, AuthUtil authUtil) {
        this.assetRepository = assetRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.fileMetadataRepository = fileMetadataRepository;
        this.ownershipValidator = ownershipValidator;
        this.assetMapper = assetMapper;
        this.authUtil = authUtil;
    }

    @Transactional
    public InventoryAssetResponse createAsset(CreateInventoryAssetRequest request, List<MultipartFile> photos) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        User owner = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Property property = null;

        if (request.propertyId() != null) {
            ownershipValidator.validateOwnership(
                    request.propertyId(), auth.id());
            property = propertyRepository
                    .findById(request.propertyId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Property not found"));
        }

        validateValuation(request);

        InventoryAsset asset = assetMapper.toEntity(request, owner, property);
        InventoryAsset saved = assetRepository.save(asset);

        if (photos == null || photos.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one photo is required"
            );
        }

        List<FileUploadResponse> uploads = fileStorageService
                .uploadMultiple(photos, FileCategory.EQUIPMENT_PHOTO,
                        saved.getId());

        List<String> photoUrls = uploads.stream()
                .map(FileUploadResponse::originalUrl)
                .toList();

        log.info("InventoryAsset created id={} photos={}",
                saved.getId(), photoUrls.size());

        return assetMapper.toResponse(saved, photoUrls);
    }

    @Transactional
    public InventoryAssetResponse updateAsset(UUID assetId, UpdateInventoryAssetRequest request) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        InventoryAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException("Asset not found"));

        if (!asset.getOwner().getId().equals(auth.id())) {
            throw new AccessDeniedException("Only the owner can edit this asset");
        }

        if (asset.getApprovedForBarter() &&
                request.referenceValue() != null) {
            throw new IllegalArgumentException(
                    "Cannot change value of approved barter asset"
            );
        }

        boolean hasChanges = false;

        if (request.description() != null) {
            asset.setDescription(request.description());
            hasChanges = true;
        }
        if (request.condition() != null) {
            asset.setCondition(request.condition());
            hasChanges = true;
        }
        if (request.quantity() != null) {
            asset.setQuantity(request.quantity());
            hasChanges = true;
        }
        if (request.referenceValue() != null) {
            asset.setReferenceValue(request.referenceValue());
            hasChanges = true;
        }
        if (request.unit() != null) {
            asset.setUnit(request.unit());
            hasChanges = true;
        }
        if (request.valuationMethod() != null) {
            asset.setValuationMethod(request.valuationMethod());
            hasChanges = true;
        }
        if (request.agreedValue() != null) {
            asset.setAgreedValue(request.agreedValue());
            hasChanges = true;
        }
        if (request.commodityReference() != null) {
            asset.setCommodityReference(request.commodityReference());
            hasChanges = true;
        }
        if (request.commodityQuantityEquivalent() != null) {
            asset.setCommodityQuantityEquivalent(
                    request.commodityQuantityEquivalent());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for asset id={}", assetId);
        }

        List<String> photoUrls = getPhotoUrls(assetId);

        log.info("InventoryAsset updated id={}", assetId);
        return assetMapper.toResponse(asset, photoUrls);
    }

    @Transactional
    public InventoryAssetResponse addPhotos(UUID assetId,
                                            List<MultipartFile> photos) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        InventoryAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Asset not found"));

        if (!asset.getOwner().getId().equals(auth.id())) {
            throw new AccessDeniedException(
                    "Only the owner can add photos"
            );
        }

        fileStorageService.uploadMultiple(
                photos, FileCategory.EQUIPMENT_PHOTO, assetId);

        List<String> photoUrls = getPhotoUrls(assetId);

        log.info("Photos added to asset id={} count={}",
                assetId, photos.size());

        return assetMapper.toResponse(asset, photoUrls);
    }

    @Transactional
    public InventoryAssetResponse requestBarterApproval(UUID assetId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        InventoryAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Asset not found"));

        if (!asset.getOwner().getId().equals(auth.id())) {
            throw new AccessDeniedException(
                    "Only the owner can request barter approval"
            );
        }

        if (asset.getApprovedForBarter()) {
            throw new IllegalArgumentException(
                    "Asset already approved for barter"
            );
        }

        List<String> photoUrls = getPhotoUrls(assetId);
        if (photoUrls.isEmpty()) {
            throw new IllegalArgumentException(
                    "Asset must have photos before requesting barter approval"
            );
        }

        log.info("Barter approval requested for asset id={}", assetId);
        return assetMapper.toResponse(asset, photoUrls);
    }

    @Transactional
    public InventoryAssetResponse approveForBarter(UUID assetId, String approvalNotes) {

        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        InventoryAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Asset not found"));

        if (asset.getApprovedForBarter()) {
            throw new IllegalArgumentException(
                    "Asset already approved"
            );
        }

        User admin = userRepository.findById(auth.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found"));

        asset.setApprovedForBarter(true);
        asset.setApprovedBy(admin);
        asset.setApprovedAt(Instant.now());
        asset.setApprovalNotes(approvalNotes);

        List<String> photoUrls = getPhotoUrls(assetId);

        log.info("InventoryAsset approved for barter id={} by={}",
                assetId, auth.id());

        return assetMapper.toResponse(asset, photoUrls);
    }

    @Transactional
    public void rejectBarterApproval(UUID assetId, String reason) {
        InventoryAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Asset not found"));

        asset.setApprovalNotes(reason);

        log.info("InventoryAsset barter rejected id={}", assetId);
    }

    @Transactional(readOnly = true)
    public Page<InventoryAssetResponse> findMyAssets(Pageable pageable) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        return assetRepository
                .findByOwner_IdAndAvailableTrue(auth.id(), pageable)
                .map(a -> assetMapper.toResponse(a, getPhotoUrls(a.getId())));
    }

    @Transactional(readOnly = true)
    public Page<InventoryAssetResponse> findBarterCatalog(Pageable pageable) {
        return assetRepository
                .findByApprovedForBarterTrueAndAvailableTrue(pageable)
                .map(a -> assetMapper.toResponse(a, getPhotoUrls(a.getId())));
    }

    @Transactional(readOnly = true)
    public Page<InventoryAssetResponse> findPendingApproval(Pageable pageable) {
        return assetRepository
                .findByApprovedForBarterFalseAndAvailableTrue(pageable)
                .map(a -> assetMapper.toResponse(a, getPhotoUrls(a.getId())));
    }

    private List<String> getPhotoUrls(UUID assetId) {
        return fileMetadataRepository
                .findByFileCategoryAndEntityIdAndDeletedFalse(
                        FileCategory.EQUIPMENT_PHOTO, assetId)
                .stream()
                .map(FileMetadata::getOriginalUrl)
                .toList();
    }

    private void validateValuation(CreateInventoryAssetRequest request) {
        switch (request.valuationMethod()) {
            case FIXED_VALUE -> {
                if (request.agreedValue() == null) {
                    throw new IllegalArgumentException(
                            "agreedValue is required for FIXED_VALUE"
                    );
                }
            }
            case COMMODITY_LINKED -> {
                if (request.commodityReference() == null ||
                        request.commodityQuantityEquivalent() == null) {
                    throw new IllegalArgumentException(
                            "commodityReference and " +
                                    "commodityQuantityEquivalent are required " +
                                    "for COMMODITY_LINKED"
                    );
                }
            }
            case MARKET_APPRAISAL -> {
                if (request.referenceValue() == null) {
                    throw new IllegalArgumentException(
                            "referenceValue is required for MARKET_APPRAISAL"
                    );
                }
            }
        }
    }
}