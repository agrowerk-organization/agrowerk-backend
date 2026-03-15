package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.inventory.CreateInputRequest;
import tech.agrowerk.application.dto.response.inventory.InputResponse;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.InputCategory;
import tech.agrowerk.infrastructure.model.supplier.Supplier;

@Component
public class InputMapper {

    public Input toEntity(CreateInputRequest request,
                          InputCategory category,
                          Supplier supplier,
                          boolean globalVisible) {
        Input input = new Input();
        input.setName(request.name());
        input.setInternalCode(request.internalCode());
        input.setManufacturerCode(request.manufacturerCode());
        input.setDescription(request.description());
        input.setUnitOfMeasure(request.unitOfMeasure());
        input.setActiveIngredient(request.activeIngredient());
        input.setFormulation(request.formulation());
        input.setConcentration(request.concentration());
        input.setMapaRegistration(request.mapaRegistration());
        input.setToxicologicalClass(request.toxicologicalClass());
        input.setGracePeriod(request.gracePeriod());
        input.setMinimumStock(request.minimumStock());
        input.setMaximumStock(request.maximumStock());
        input.setCategory(category);
        input.setSupplier(supplier);
        input.setGlobalVisible(globalVisible);
        input.setControlled(
                request.controlled() != null && request.controlled());
        input.setActive(true);
        return input;
    }

    public InputResponse toResponse(Input input) {
        return new InputResponse(
                input.getId(),
                input.getName(),
                input.getInternalCode(),
                input.getManufacturerCode(),
                input.getDescription(),
                input.getUnitOfMeasure().name(),
                input.getActiveIngredient(),
                input.getFormulation(),
                input.getConcentration(),
                input.getMapaRegistration(),
                input.getToxicologicalClass() != null
                        ? input.getToxicologicalClass().name() : null,
                input.getGracePeriod(),
                input.getMinimumStock(),
                input.getMaximumStock(),
                input.getAveragePurchasePrice(),
                input.getLastPurchasePrice(),
                input.getActive(),
                input.getControlled(),
                input.getGlobalVisible(),
                input.getCategory().getId(),
                input.getCategory().getName(),
                input.getSupplier() != null
                        ? input.getSupplier().getId() : null,
                input.getSupplier() != null
                        ? input.getSupplier().getCorporateReason() : null,
                input.getCreatedAt(),
                input.getUpdatedAt()
        );
    }
}