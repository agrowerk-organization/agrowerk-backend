package tech.agrowerk.infrastructure.exception.global;


import java.util.List;

public record ErrorResponse(String message, List<String> errors) {
    public ErrorResponse(String message) {
        this(message, List.of());
    }
}
