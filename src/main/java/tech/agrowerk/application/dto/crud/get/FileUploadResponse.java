package tech.agrowerk.application.dto.crud.get;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileUploadResponse(
        java.util.UUID id,
        String originalUrl,
        String optmizedUrl,
        String thumbnailUrl,
        String mediumUrl,
        String originalFileName,
        String contentType,
        Long fileSize,
        Integer width,
        Integer height,
        String category,
        LocalDateTime createdAt
) {
    public static FileUploadResponse simple(UUID id, String originalUrl, String thumbnailUrl) {
        return new FileUploadResponse(
                id,
                originalUrl,
                null,
                thumbnailUrl,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null

        );
    }
}
