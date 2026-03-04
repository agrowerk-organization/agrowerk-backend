package tech.agrowerk.application.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateCropVarietyRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 200)
        String description,

        @Size(max = 100)
        String region
) {}