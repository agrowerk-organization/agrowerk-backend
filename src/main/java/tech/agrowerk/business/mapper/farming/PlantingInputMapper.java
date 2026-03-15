package tech.agrowerk.business.mapper.farming;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreatePlantingInputRequest;
import tech.agrowerk.application.dto.response.farming.PlantingInputResponse;
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
        plantingInput.setUnitOfMeasure(request.unitOfMeasure());
        plantingInput.setApplicationDate(request.applicationDate());
        return plantingInput;
    }

    public PlantingInputResponse toResponse(PlantingInput plantingInput) {
        return new PlantingInputResponse(
                plantingInput.getId(),
                plantingInput.getPlanting().getId(),
                plantingInput.getInput().getId(),
                plantingInput.getInput().getName(),
                plantingInput.getUnitOfMeasure().toString(),
                plantingInput.getQuantity(),
                plantingInput.getApplicationDate(),
                plantingInput.getCreatedAt()
        );
    }
}