package tech.agrowerk.application.dto.response.farming;

import java.time.Instant;
import java.util.UUID;

public record CropResponse(
    UUID id,
    String name,
    String scientificName,
    int growthCycleDays,
    String cropCategory,
    Instant createdAt,
    Instant updatedAt
) {
}
