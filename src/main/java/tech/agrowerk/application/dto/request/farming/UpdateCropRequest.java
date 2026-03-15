package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.CropCategory;

public record UpdateCropRequest(
        @Size(max = 255)
        String name,

        @Size(max = 255)
        String scientificName,

        @Positive
        Integer growthCycleDays,

        CropCategory cropCategory
) {}
