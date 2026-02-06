package tech.agrowerk.application.controller.file;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.business.service.file.FileStorageService;
import tech.agrowerk.application.dto.crud.get.FileUploadResponse;
import tech.agrowerk.infrastructure.model.file.enums.FileCategory;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("category") @NotNull FileCategory category,
            @RequestParam(value = "entityId", required = false) UUID entityId) {


        FileUploadResponse response = fileStorageService.upload(file, category, entityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/upload/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") @NotNull List<MultipartFile> files,
            @RequestParam("category") @NotNull FileCategory category,
            @RequestParam(value = "entityId", required = false) UUID entityId) {

        List<FileUploadResponse> responses = fileStorageService.uploadMultiple(files, category, entityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> getFile(@PathVariable UUID id) {
        FileUploadResponse response = fileStorageService.getFileById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public-id/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FileUploadResponse> getFileByPublicId(@PathVariable String publicId) {
        FileUploadResponse response = fileStorageService.getFileByPublicId(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FileUploadResponse>> listFiles(
            @RequestParam FileCategory category,
            @RequestParam(required = false) UUID entityId) {

        List<FileUploadResponse> responses = fileStorageService.listFiles(category, entityId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileStorageService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> hardDeleteFile(@PathVariable UUID id) {
        fileStorageService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<String> cleanupOldFiles(
            @RequestParam(defaultValue = "30") int daysOld) {

        int deletedCount = fileStorageService.cleanupOldDeletedFiles(daysOld);
        return ResponseEntity.ok(deletedCount + " Removed files");
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<FileStorageService.StorageStats> getStats() {
        FileStorageService.StorageStats stats = fileStorageService.getStats();
        return ResponseEntity.ok(stats);
    }
}