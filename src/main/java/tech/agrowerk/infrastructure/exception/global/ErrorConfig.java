package tech.agrowerk.infrastructure.exception.global;

import org.springframework.http.HttpStatus;

public record ErrorConfig(
        HttpStatus status,
        String logMessage,
        String friendlyMessage
) {}
