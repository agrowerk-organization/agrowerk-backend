package tech.agrowerk.infrastructure.exception.global;

import lombok.*;

@Getter
@Setter
public class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
