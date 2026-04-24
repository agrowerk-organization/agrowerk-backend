package tech.agrowerk.infrastructure.exception.local;

public class PdfGenerationException extends RuntimeException {
    public PdfGenerationException(String message) {
        super(message);
    }
}
