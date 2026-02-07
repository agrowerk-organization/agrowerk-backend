package tech.agrowerk.infrastructure.exception.global;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    private final String message;

    private final Map<String, String> validationErrors;

    private final List<String> errors;

    private final String path;

    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.validationErrors = null;
        this.errors = null;
        this.path = null;
    }

    public ErrorResponse(String message, Map<String, String> validationErrors) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.validationErrors = validationErrors;
        this.errors = null;
        this.path = null;
    }

    public ErrorResponse(String message, List<String> errors) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.validationErrors = null;
        this.errors = errors;
        this.path = null;
    }

    public ErrorResponse(String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.validationErrors = null;
        this.errors = null;
        this.path = path;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private Map<String, String> validationErrors;
        private List<String> errors;
        private String path;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder validationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public ErrorResponse build() {
            ErrorResponse response = new ErrorResponse(message);
            return response;
        }
    }
}