package tech.agrowerk.infrastructure.exception.local;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
