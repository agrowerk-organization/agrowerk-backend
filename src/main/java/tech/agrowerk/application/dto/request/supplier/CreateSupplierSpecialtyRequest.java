package tech.agrowerk.application.dto.request.supplier;

import jakarta.validation.constraints.*;

public record CreateSupplierSpecialtyRequest(
        @NotBlank @Size(max = 255)
        String name,

        String description
) {}