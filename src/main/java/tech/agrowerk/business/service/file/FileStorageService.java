package tech.agrowerk.business.service.file;

import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.crud.get.FileUploadResponse;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {

    FileUploadResponse upload(MultipartFile file, FileCategory fileCategory, UUID entityId);

    List<FileUploadResponse> uploadMultiple(List<MultipartFile> files, FileCategory fileCategory, UUID entityId);

    FileUploadResponse getFileById(UUID id);

    FileUploadResponse getFileByPublicId(String publicId);

    List<FileUploadResponse> listFiles(FileCategory category, UUID entityId);

    void delete(UUID id);

    void hardDelete(UUID id);

    int cleanupOldDeletedFiles(int daysOld);

    StorageStats getStats();

    record StorageStats(
            long totalFiles,
            long totalSizeBytes,
            long totalSizeMB,
            long propertyPhotos,
            long productPhotos,
            long documents
    ) {}
}
