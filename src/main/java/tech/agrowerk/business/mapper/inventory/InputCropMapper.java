package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.inventory.CreateInputCropRequest;
import tech.agrowerk.application.dto.response.inventory.InputCropResponse;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.InputCrop;

@Component
public class InputCropMapper {

    public InputCrop toEntity(CreateInputCropRequest request,
                              Input input,
                              Crop crop) {
        InputCrop inputCrop = new InputCrop();
        inputCrop.setInput(input);
        inputCrop.setCrop(crop);
        inputCrop.setUsageRecommendation(request.usageRecommendation());
        inputCrop.setRecommendedDosePerHectare(
                request.recommendedDosePerHectare());
        inputCrop.setDoseUnit(request.unitOfMeasure());
        inputCrop.setApprovedByAdmin(false);
        return inputCrop;
    }

    public InputCropResponse toResponse(InputCrop inputCrop) {
        return new InputCropResponse(
                inputCrop.getId(),
                inputCrop.getInput().getId(),
                inputCrop.getInput().getName(),
                inputCrop.getInput().getCategory().getName(),
                inputCrop.getCrop().getId(),
                inputCrop.getCrop().getName(),
                inputCrop.getUsageRecommendation(),
                inputCrop.getRecommendedDosePerHectare(),
                inputCrop.getDoseUnit() != null
                        ? inputCrop.getDoseUnit().name() : null,
                inputCrop.getApprovedByAdmin(),
                inputCrop.getApprovedBy() != null
                        ? inputCrop.getApprovedBy().getId() : null,
                inputCrop.getApprovedBy() != null
                        ? inputCrop.getApprovedBy().getName() : null,
                inputCrop.getApprovedAt(),
                inputCrop.getCreatedAt()
        );
    }
}
