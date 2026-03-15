package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.inventory.enums.HazardLevel;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;

public record UpdateInputCategoryRequest(
        @Size(max = 100)
        String name,

        String description,

        UnitOfMeasure unitOfMeasure,

        @Size(max = 50)
        String icon,

        @Size(max = 7)
        String color,

        HazardLevel hazardLevel
) {}