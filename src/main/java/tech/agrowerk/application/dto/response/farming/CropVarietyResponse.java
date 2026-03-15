package tech.agrowerk.application.dto.response.farming;

import java.time.Instant;
import java.util.UUID;

public record CropVarietyResponse(
    UUID id,
    String name,
    String description,
    String region,
    UUID cropId,
    String cropName,
    UUID userId,
    String userName,
    Instant createdAt,
    Instant updatedAt
) {
}
