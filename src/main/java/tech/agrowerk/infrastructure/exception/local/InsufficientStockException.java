package tech.agrowerk.infrastructure.exception.local;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
