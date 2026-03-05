package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreatePlantingInputRequest;
import tech.agrowerk.application.dto.response.PlantingInputResponse;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.PlantingInput;
import tech.agrowerk.infrastructure.model.inventory.Input;

@Component
public class PlantingInputMapper {

    public PlantingInput toEntity(CreatePlantingInputRequest request,
                                  Planting planting,
                                  Input input) {
        PlantingInput plantingInput = new PlantingInput();
        plantingInput.setPlanting(planting);
        plantingInput.setInput(input);
        plantingInput.setQuantity(request.quantity());
        plantingInput.setUnit(input.getUnitOfMeasure().name());
        plantingInput.setApplicationDate(request.applicationDate());
        return plantingInput;
    }

    public PlantingInputResponse toResponse(PlantingInput plantingInput) {
        return new PlantingInputResponse(
                plantingInput.getId(),
                plantingInput.getPlanting().getId(),
                plantingInput.getInput().getId(),
                plantingInput.getInput().getName(),
                plantingInput.getUnit(),
                plantingInput.getQuantity(),
                plantingInput.getApplicationDate(),
                plantingInput.getCreatedAt()
        );
    }
}