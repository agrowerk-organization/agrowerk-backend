package tech.agrowerk.application.dto.response.inventory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InputCategoryResponse(
        UUID id,
        String name,
        String description,
        String unitOfMeasure,
        String icon,
        String color,
        String hazardLevel,
        Integer level,
        Boolean isActive,
        Boolean requiresLicense,
        UUID parentId,
        List<InputCategoryResponse> children,
        Instant createdAt,
        Instant updatedAt
) {}