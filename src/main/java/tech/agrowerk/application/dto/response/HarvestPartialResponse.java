package tech.agrowerk.application.dto.response;


import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HarvestPartialResponse(
        UUID id,
        UUID harvestId,
        LocalDate partialDate,
        BigDecimal quantityKg,
        String qualityGrade,
        String notes,
        UUID responsibleUserId,
        String responsibleUserName,
        BigDecimal currentQuantityKg,
        Instant createdAt
) {}