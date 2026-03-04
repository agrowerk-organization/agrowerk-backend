package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.farming.enums.CropCategory;

public record CreateCropRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @Size(max = 255)
        String scientificName,

        @NotNull
        @Positive
        int growthCycleDays,

        @NotNull
        CropCategory cropCategory
) {}