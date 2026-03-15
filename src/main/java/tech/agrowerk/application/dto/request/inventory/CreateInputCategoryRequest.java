package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.inventory.enums.HazardLevel;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

import java.util.UUID;

public record CreateInputCategoryRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        String description,

        @NotNull
        UnitOfMeasure unitOfMeasure,

        @Size(max = 50)
        String icon,

        @Size(max = 7)
        String color,

        UUID parentId,

        @NotNull
        HazardLevel hazardLevel
) {
}
