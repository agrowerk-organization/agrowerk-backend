package tech.agrowerk.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AgriculturalPracticeResponse(
        UUID id,
        UUID plantingId,
        String cropName,
        String fieldName,
        String practipeType,
        LocalDate applicationDate,
        String productUsed,
        BigDecimal quantityUsed,
        String unitOfMeasure,
        BigDecimal costAmount,
        UUID responsibleUserId,
        String responsibleUserName,
        String observations,
        Instant createdAt
) {
}
