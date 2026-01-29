package tech.agrowerk.business.validators;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tech.agrowerk.infrastructure.model.file.enums.FileStorageErrorCode;
import tech.agrowerk.infrastructure.exception.local.FileStorageException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


@Slf4j
@Component
public class FileUploadValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46};
    private static final byte[] WEBP_MAGIC = new byte[]{0x52, 0x49, 0x46, 0x46};

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "dll", "jar", "php", "jsp", "asp", "aspx"
    );


    public void validate(MultipartFile file) {
        validateNotEmpty(file);
        validateSize(file);
        validateExtension(file);
        validateMimeType(file);
        validateMagicBytes(file);
    }

    private void validateNotEmpty(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException(
                    FileStorageErrorCode.EMPTY_FILE,
                    "Empty file");
        }
    }

    private void validateSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            long sizeMB = file.getSize() / (1024 * 1024);
            throw new FileStorageException(
                    FileStorageErrorCode.FILE_TOO_LARGE,
                    String.format("File too large: %dMB. Maximum allowed: 10MB", sizeMB));
        }
    }

    private void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isEmpty()) {
            throw new FileStorageException(
                    FileStorageErrorCode.STORAGE_INVALID,
                    "File name not identified");
        }

        String extension = getFileExtension(filename).toLowerCase();

        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            throw new FileStorageException(
                    FileStorageErrorCode.MIME_TYPE_NOT_ALLOWED,
                    "File type not allowed: " + extension);
        }
    }

    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new FileStorageException(
                    FileStorageErrorCode.MIME_TYPE_NOT_IDENTIFIED,
                    "File type not identified");
        }

        List<String> allowedTypes = Arrays.asList(
                "image/jpeg",
                "image/jpg",
                "image/png",
                "image/webp",
                "application/pdf",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        if (!allowedTypes.contains(contentType)) {
            throw new FileStorageException(
                    FileStorageErrorCode.MIME_TYPE_NOT_ALLOWED,
                    "File type not allowed: " + contentType
            );
        }
    }


    private void validateMagicBytes(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);

            if (bytesRead < 4) {
                throw new FileStorageException(
                        FileStorageErrorCode.STORAGE_INVALID,
                        "Corrupted or invalid file");
            }

            boolean isValid = isJpeg(header) || isPng(header) || isPdf(header) || isWebp(header);

            if (!isValid) {
                throw new FileStorageException(
                        FileStorageErrorCode.INVALID_EXTENSION,
                        "File does not match the declared type");
            }

        } catch (IOException e) {
            throw new FileStorageException(
                    FileStorageErrorCode.STORAGE_INVALID,
                    "Erro ao validar arquivo", e);
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3 &&
                header[0] == JPEG_MAGIC[0] &&
                header[1] == JPEG_MAGIC[1] &&
                header[2] == JPEG_MAGIC[2];
    }

    private boolean isPng(byte[] header) {
        return header.length >= 4 &&
                header[0] == PNG_MAGIC[0] &&
                header[1] == PNG_MAGIC[1] &&
                header[2] == PNG_MAGIC[2] &&
                header[3] == PNG_MAGIC[3];
    }

    private boolean isPdf(byte[] header) {
        return header.length >= 4 &&
                header[0] == PDF_MAGIC[0] &&
                header[1] == PDF_MAGIC[1] &&
                header[2] == PDF_MAGIC[2] &&
                header[3] == PDF_MAGIC[3];
    }

    private boolean isWebp(byte[] header) {
        return header.length >= 4 &&
                header[0] == WEBP_MAGIC[0] &&
                header[1] == WEBP_MAGIC[1] &&
                header[2] == WEBP_MAGIC[2] &&
                header[3] == WEBP_MAGIC[3];
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}