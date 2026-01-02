package tech.agrowerk.infrastructure.exception.local;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
