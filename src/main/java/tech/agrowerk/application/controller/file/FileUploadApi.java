package tech.agrowerk.application.controller.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.crud.get.FileUploadResponse;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;

import java.util.List;
import java.util.UUID;

@Tag(name = "Files", description = "Management of file uploads, retrieval, and storage cleanup")
public interface FileUploadApi {

    @Operation(summary = "Upload a single file", description = "Uploads a file to the storage provider and associates it with a category.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or category")
    })
    ResponseEntity<FileUploadResponse> uploadFile(
            MultipartFile file,
            FileCategory category,
            UUID entityId
    );

    @Operation(summary = "Upload multiple files", description = "Bulk upload for multiple files under the same category.")
    ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            List<MultipartFile> files,
            FileCategory category,
            UUID entityId
    );

    @Operation(summary = "Get file metadata by ID", description = "Retrieves database information for a specific file.")
    ResponseEntity<FileUploadResponse> getFile(UUID id);

    @Operation(summary = "Get file by public ID", description = "Retrieves file information using its public string identifier.")
    ResponseEntity<FileUploadResponse> getFileByPublicId(String publicId);

    @Operation(summary = "List files", description = "Lists all files filtered by category and/or entity ID.")
    ResponseEntity<List<FileUploadResponse>> listFiles(FileCategory category, UUID entityId);

    @Operation(summary = "Delete file (Soft)", description = "Marks a file as deleted in the system.")
    ResponseEntity<Void> deleteFile(UUID id);

    @Operation(summary = "Hard delete file", description = "Permanently removes a file from the storage and database. Admin only.")
    ResponseEntity<Void> hardDeleteFile(UUID id);

    @Operation(summary = "Cleanup old files", description = "Admin utility to purge files marked as deleted for a certain amount of time.")
    ResponseEntity<String> cleanupOldFiles(int daysOld);

    @Operation(summary = "Storage statistics", description = "Retrieves storage usage metrics. Admin only.")
    ResponseEntity<FileStorageService.StorageStats> getStats();
}