package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InputCropResponse(
        UUID id,
        UUID inputId,
        String inputName,
        String inputCategory,
        UUID cropId,
        String cropName,
        String usageRecommendation,
        BigDecimal recommendedDosePerHectare,
        String doseUnit,
        Boolean approvedByAdmin,
        UUID approvedById,
        String approvedByName,
        Instant approvedAt,
        Instant createdAt
) {}