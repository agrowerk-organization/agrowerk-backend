package tech.agrowerk.infrastructure.exception.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tech.agrowerk.infrastructure.exception.local.*;
import tech.agrowerk.infrastructure.exception.local.IllegalArgumentException;
import tech.agrowerk.infrastructure.exception.local.IllegalStateException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class AdvancedGlobalExceptionHandler {

    private static final Map<Class<? extends Exception>, ErrorConfig> ERROR_REGISTRY = Map.ofEntries(
            entry(BadCredentialsException.class, badRequest("Invalid credentials provided")),
            entry(IllegalArgumentException.class, badRequest("Invalid argument provided")),
            entry(InvalidTokenException.class, badRequest("Invalid or expired token")),
            entry(InvalidPasswordException.class, badRequest("Password does not meet requirements")),
            entry(AccessDeniedException.class, forbidden("Access denied to this resource")),
            entry(OperationDeniedException.class, forbidden("Operation not permitted")),
            entry(EntityNotFoundException.class, notFound("Requested resource not found")),
            entry(EntityAlreadyExistsException.class, conflict("Resource already exists")),
            entry(IllegalStateException.class, conflict("Operation not allowed in current state")),
            entry(FileStorageException.class, internalError("File operation failed")),
            entry(AuthenticationException.class, internalError("Authentication system error")),
            entry(WeatherApiException.class, serviceUnavailable("Weather service temporarily unavailable")),
            entry(WeatherAlertException.class, notFound("Alert not found"))
    );

    @ExceptionHandler({
            BadCredentialsException.class,
            IllegalArgumentException.class,
            InvalidTokenException.class,
            InvalidPasswordException.class,
            AccessDeniedException.class,
            OperationDeniedException.class,
            EntityNotFoundException.class,
            EntityAlreadyExistsException.class,
            IllegalStateException.class,
            FileStorageException.class,
            AuthenticationException.class,
            WeatherApiException.class,
            WeatherAlertException.class
    })
    public ResponseEntity<ErrorResponse> handleMappedException(Exception ex) {
        ErrorConfig config = ERROR_REGISTRY.get(ex.getClass());

        if (config == null) {
            log.error("Unmapped exception: {}", ex.getClass().getName(), ex);
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred", ex);
        }

        if (config.status().is5xxServerError()) {
            log.error("{}: {}", config.logMessage(), ex.getMessage(), ex);
        } else if (config.status().is4xxClientError()) {
            log.warn("{}: {}", config.logMessage(), ex.getMessage());
        }

        return buildResponse(config.status(),
                config.friendlyMessage() != null ? config.friendlyMessage() : ex.getMessage(),
                ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof org.springframework.validation.FieldError
                    ? ((org.springframework.validation.FieldError) error).getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        ErrorResponse response = new ErrorResponse("Validation failed", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse response = new ErrorResponse(
                "An unexpected error occurred. Please contact support.",
                Map.of("exceptionType", ex.getClass().getSimpleName())
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, Exception ex) {
        ErrorResponse response = new ErrorResponse(message);
        return new ResponseEntity<>(response, status);
    }

    // Factory methods para ErrorConfig
    private static Map.Entry<Class<? extends Exception>, ErrorConfig> entry(
            Class<? extends Exception> exClass,
            ErrorConfig config) {
        return Map.entry(exClass, config);
    }

    private static ErrorConfig badRequest(String friendly) {
        return new ErrorConfig(HttpStatus.BAD_REQUEST, "Bad Request", friendly);
    }

    private static ErrorConfig forbidden(String friendly) {
        return new ErrorConfig(HttpStatus.FORBIDDEN, "Access Forbidden", friendly);
    }

    private static ErrorConfig notFound(String friendly) {
        return new ErrorConfig(HttpStatus.NOT_FOUND, "Resource Not Found", friendly);
    }

    private static ErrorConfig conflict(String friendly) {
        return new ErrorConfig(HttpStatus.CONFLICT, "Resource Conflict", friendly);
    }

    private static ErrorConfig internalError(String friendly) {
        return new ErrorConfig(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", friendly);
    }

    private static ErrorConfig serviceUnavailable(String friendly) {
        return new ErrorConfig(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", friendly);
    }
}