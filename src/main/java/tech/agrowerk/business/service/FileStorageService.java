package tech.agrowerk.business.service;

import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.crud.get.FileUploadResponse;
import tech.agrowerk.infrastructure.enums.FileCategory;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    FileUploadResponse upload(MultipartFile file, FileCategory fileCategory, Long entityId);

    List<FileUploadResponse> uploadMultiple(List<MultipartFile> files, FileCategory fileCategory, Long entityId);

    FileUploadResponse getFileById(Long id);

    FileUploadResponse getFileByPublicId(String publicId);

    List<FileUploadResponse> listFiles(FileCategory category, Long entityId);

    void delete(Long id);

    void hardDelete(Long id);

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
