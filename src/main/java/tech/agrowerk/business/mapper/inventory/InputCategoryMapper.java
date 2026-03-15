package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.inventory.CreateInputCategoryRequest;
import tech.agrowerk.application.dto.response.inventory.InputCategoryResponse;
import tech.agrowerk.infrastructure.model.inventory.InputCategory;

import java.util.List;

@Component
public class InputCategoryMapper {

    public InputCategory toEntity(CreateInputCategoryRequest request,
                                  InputCategory parent) {
        InputCategory category = new InputCategory();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setUnitOfMeasure(request.unitOfMeasure());
        category.setIcon(request.icon());
        category.setColor(request.color());
        category.setHazardLevel(request.hazardLevel());
        category.setParent(parent);
        category.setLevel(parent != null ? parent.getLevel() + 1 : 0);
        category.setIsActive(true);
        return category;
    }

    public InputCategoryResponse toResponse(InputCategory category) {
        List<InputCategoryResponse> children = category.getChildren() != null
                ? category.getChildren().stream()
                .filter(InputCategory::getIsActive)
                .map(this::toResponse)
                .toList()
                : List.of();

        return new InputCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getUnitOfMeasure().name(),
                category.getIcon(),
                category.getColor(),
                category.getHazardLevel().name(),
                category.getLevel(),
                category.getIsActive(),
                category.getRequiresLicense(),
                category.getParent() != null
                        ? category.getParent().getId() : null,
                children,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public InputCategoryResponse toFlatResponse(InputCategory category) {
        return new InputCategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getUnitOfMeasure().name(),
                category.getIcon(),
                category.getColor(),
                category.getHazardLevel().name(),
                category.getLevel(),
                category.getIsActive(),
                category.getRequiresLicense(),
                category.getParent() != null
                        ? category.getParent().getId() : null,
                List.of(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}