package tech.agrowerk.application.dto.response.farming;

import tech.agrowerk.infrastructure.model.farming.enums.SeasonStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SeasonResponse(
        UUID id,
        String name,
        UUID propertyId,
        String propertyName,
        LocalDate startDate,
        LocalDate endDate,
        SeasonStatus seasonStatus,
        Instant createdAt
) {
}