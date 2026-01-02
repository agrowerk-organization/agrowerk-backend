package tech.agrowerk.infrastructure.exception.local;

public class OperationDeniedException extends IllegalStateException {
    public OperationDeniedException(String message) {
        super(message);
    }
}
