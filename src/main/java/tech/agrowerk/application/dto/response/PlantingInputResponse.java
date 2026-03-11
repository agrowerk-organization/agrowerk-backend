package tech.agrowerk.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PlantingInputResponse(
        UUID id,
        UUID plantingId,
        UUID inputId,
        String inputName,
        String measureUnit,
        BigDecimal quantity,
        LocalDate applicationDate,
        Instant createdAt
) {}