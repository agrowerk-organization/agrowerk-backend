package tech.agrowerk.application.controller.file;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.application.dto.response.FileUploadResponse;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController implements FileUploadApi {

    private final FileStorageService fileStorageService;

    @Override
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") FileCategory category,
            @RequestParam(value = "entityId", required = false) UUID entityId) {

        FileUploadResponse response = fileStorageService.upload(file, category, entityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("category") FileCategory category,
            @RequestParam(value = "entityId", required = false) UUID entityId) {

        List<FileUploadResponse> responses = fileStorageService.uploadMultiple(files, category, entityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> getFile(@PathVariable UUID id) {
        return ResponseEntity.ok(fileStorageService.getFileById(id));
    }

    @Override
    @GetMapping("/public-id/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> getFileByPublicId(@PathVariable String publicId) {
        return ResponseEntity.ok(fileStorageService.getFileByPublicId(publicId));
    }

    @Override
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileUploadResponse>> listFiles(
            @RequestParam FileCategory category,
            @RequestParam(required = false) UUID entityId) {
        return ResponseEntity.ok(fileStorageService.listFiles(category, entityId));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileStorageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> hardDeleteFile(@PathVariable UUID id) {
        fileStorageService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<String> cleanupOldFiles(
            @RequestParam(defaultValue = "30") int daysOld) {
        int deletedCount = fileStorageService.cleanupOldDeletedFiles(daysOld);
        return ResponseEntity.ok(deletedCount + " Removed files");
    }

    @Override
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<FileStorageService.StorageStats> getStats() {
        return ResponseEntity.ok(fileStorageService.getStats());
    }
}