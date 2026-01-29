package tech.agrowerk.infrastructure.exception.local;

import lombok.Getter;
import tech.agrowerk.infrastructure.model.file.enums.FileStorageErrorCode;

@Getter
public class FileStorageException extends RuntimeException {

    private final FileStorageErrorCode code;

    public FileStorageException(FileStorageErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public FileStorageException(FileStorageErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
