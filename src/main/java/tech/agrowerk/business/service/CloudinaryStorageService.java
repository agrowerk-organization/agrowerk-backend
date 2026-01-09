package tech.agrowerk.business.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.crud.get.FileUploadResponse;
import tech.agrowerk.infrastructure.enums.FileCategory;
import tech.agrowerk.infrastructure.enums.FileStorageErrorCode;
import tech.agrowerk.infrastructure.exception.local.FileStorageException;
import tech.agrowerk.infrastructure.model.FileMetadata;
import tech.agrowerk.infrastructure.repository.FileMetadataRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;
    private final FileMetadataRepository fileMetadataRepository;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/jpg", "image/webp"
    );
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    @Override
    @Transactional
    public FileUploadResponse upload(MultipartFile file, FileCategory category, Long entityId) {

        validateFile(file, category);

        try {
            String publicId = generatePublicId(category);
            String folder = getFolderByCategory(category);

            Map uploadParams = ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", folder,
                    "resource_type", "auto",
                    "overwrite", false
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            FileMetadata metadata = buildMetadata(uploadResult, file, category, entityId);
            metadata = fileMetadataRepository.save(metadata);

            return mapToResponse(metadata);

        } catch(IOException e) {
            throw new FileStorageException(
                    FileStorageErrorCode.STORAGE_FAILURE,
                    "Error when upload", e);
        }
    }

    @Override
    @Transactional
    public List<FileUploadResponse> uploadMultiple(List<MultipartFile> files, FileCategory category, Long entityId) {
        return files.stream()
                .map(file -> upload(file, category, entityId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FileUploadResponse getFileById(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .filter(f -> !f.isDeleted())
                .orElseThrow(() -> new FileStorageException(
                        FileStorageErrorCode.FILE_NOT_FOUND,
                        "File not found: "));
        return mapToResponse(metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public FileUploadResponse getFileByPublicId(String publicId) {
        FileMetadata metadata = fileMetadataRepository.findByCloudinaryPublicIdAndDeletedFalse(publicId)
                .orElseThrow(() -> new FileStorageException(
                        FileStorageErrorCode.FILE_NOT_FOUND,
                        "File not found"));
        return mapToResponse(metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileUploadResponse> listFiles(FileCategory category, Long entityId) {
        List<FileMetadata> files = fileMetadataRepository.findByFileCategoryAndEntityIdAndDeletedFalse(category, entityId);
        return files.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileStorageException(
                        FileStorageErrorCode.FILE_NOT_FOUND,
                        "File not found"
                ));

        metadata.markAsDeleted();
        fileMetadataRepository.save(metadata);
    }

    @Override
    @Transactional
    public void hardDelete(Long id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new FileStorageException(
                        FileStorageErrorCode.FILE_NOT_FOUND,
                        "File not found"
                ));

        try {
            cloudinary.uploader().destroy(metadata.getCloudinaryPublicId(), ObjectUtils.emptyMap());
            fileMetadataRepository.delete(metadata);
        } catch (IOException e) {
            throw new FileStorageException(
                    FileStorageErrorCode.FILE_NOT_FOUND,
                    "File not found"
                );
        }
    }

    @Override
    @Transactional
    public int cleanupOldDeletedFiles(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<FileMetadata> oldFiles = fileMetadataRepository.findDeletedFilesBefore(cutoffDate);

        int count = 0;
        for (FileMetadata file : oldFiles) {
            try {
                cloudinary.uploader().destroy(file.getCloudinaryPublicId(), ObjectUtils.emptyMap());
                fileMetadataRepository.delete(file);
                count++;
            } catch (IOException e) {
                throw new FileStorageException(
                        FileStorageErrorCode.FILE_NOT_FOUND,
                        "Error cleaning the file"
                );
            }
        }

        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public StorageStats getStats() {
        long totalFiles = fileMetadataRepository.count();
        Long totalBytes = fileMetadataRepository.getTotalStorageUsed();
        long totalMB = totalBytes / (1024 * 1024);

        long propertyPhotos = fileMetadataRepository.countByFileCategoryAndDeletedFalse(FileCategory.PROPERTY_PHOTO);
        long productPhotos = fileMetadataRepository.countByFileCategoryAndDeletedFalse(FileCategory.PRODUCT_PHOTO);
        long documents = fileMetadataRepository.countByFileCategoryAndDeletedFalse(FileCategory.DOCUMENT);

        return new StorageStats(totalFiles, totalBytes, totalMB, propertyPhotos, productPhotos, documents);
    }

    private void validateFile(MultipartFile file, FileCategory category) {
        if (file.isEmpty()) {
            throw new FileStorageException(
                FileStorageErrorCode.FILE_NOT_FOUND,
                "File not found"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException(
                    FileStorageErrorCode.FILE_TOO_LARGE,
                    String.format("File too large: %dMB. Maximum allowed: 10MB", file.getSize())
            );
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileStorageException(
                    FileStorageErrorCode.MIME_TYPE_NOT_IDENTIFIED,
                    "File mime type not identified"
            );
        }

        boolean isImage = ALLOWED_IMAGE_TYPES.contains(contentType);
        boolean isDocument = ALLOWED_DOCUMENT_TYPES.contains(contentType);

        if (category == FileCategory.DOCUMENT && !isDocument) {
            throw new FileStorageException(
                    FileStorageErrorCode.MIME_TYPE_NOT_ALLOWED,
                    "Type of document not allowed:"
            );
        }

        if (category != FileCategory.DOCUMENT && !isImage) {
            throw new FileStorageException(
                    FileStorageErrorCode.MIME_TYPE_NOT_ALLOWED,
                    "Type of image not allowed: "
            );
        }
    }

    private String generatePublicId(FileCategory category) {
        return category.name().toLowerCase() + "_" + UUID.randomUUID().toString();
    }

    private String getFolderByCategory(FileCategory category) {
        return switch (category) {
            case PROPERTY_PHOTO -> "agrowerk/properties";
            case PRODUCT_PHOTO -> "agrowerk/products";
            case EQUIPMENT_PHOTO -> "agrowerk/equipment";
            case USER_AVATAR -> "agrowerk/users";
            case DOCUMENT, INVOICE -> "agrowerk/documents";
            case REPORT -> "agrowerk/reports";
            case SOIL_ANALYSIS -> "agrowerk/soil-analysis";
            case OTHER -> "agrowerk/other";
        };
    }

    private FileMetadata buildMetadata(Map uploadResult, MultipartFile file,
                                       FileCategory category, Long entityId) {
        String publicId = (String) uploadResult.get("public_id");
        String secureUrl = (String) uploadResult.get("secure_url");

        Integer width = uploadResult.get("width") != null ? (Integer) uploadResult.get("width") : null;
        Integer height = uploadResult.get("height") != null ? (Integer) uploadResult.get("height") : null;

        String thumbnailUrl = generateTransformationUrl(publicId, "c_thumb,w_200,h_200,g_face");
        String mediumUrl = generateTransformationUrl(publicId, "c_limit,w_800,h_600,q_auto");

        return FileMetadata.builder()
                .cloudinaryPublicId(publicId)
                .originalUrl(secureUrl)
                .thumbnailUrl(thumbnailUrl)
                .mediumUrl(mediumUrl)
                .originalFileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .width(width)
                .height(height)
                .fileCategory(category)
                .entityId(entityId)
                .deleted(false)
                .build();
    }

    private String generateTransformationUrl(String publicId, String transformation) {
        return cloudinary.url()
                .transformation()
                .zoom("auto")
                .quality("auto")
                .generate();
    }

    private FileUploadResponse mapToResponse(FileMetadata metadata) {
        String optimizedUrl = generateTransformationUrl(
                metadata.getCloudinaryPublicId(),
                "f_auto,q_auto"
        );

        return new FileUploadResponse(
                metadata.getId(),
                metadata.getOriginalUrl(),
                optimizedUrl,
                metadata.getThumbnailUrl(),
                metadata.getMediumUrl(),
                metadata.getOriginalFileName(),
                metadata.getContentType(),
                metadata.getFileSize(),
                metadata.getWidth(),
                metadata.getHeight(),
                metadata.getFileCategory().toString(),
                metadata.getCreatedAt()
        );
    }
}