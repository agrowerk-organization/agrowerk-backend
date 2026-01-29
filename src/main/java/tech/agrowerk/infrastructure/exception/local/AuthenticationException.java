package tech.agrowerk.infrastructure.exception.local;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
