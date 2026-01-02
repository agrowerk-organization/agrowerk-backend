package tech.agrowerk.infrastructure.exception.local;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
