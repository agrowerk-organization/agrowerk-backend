package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCropVarietyRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 200)
        String description,

        @Size(max = 100)
        String region
) {}