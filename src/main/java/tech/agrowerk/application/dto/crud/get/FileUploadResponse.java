package tech.agrowerk.application.dto.crud.get;

import jdk.jshell.Snippet;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FileUploadResponse(
        Long id,
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
    public static FileUploadResponse simple(Long id, String originalUrl, String thumbnailUrl) {
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
