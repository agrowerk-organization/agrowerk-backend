package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreateCropRequest;
import tech.agrowerk.application.dto.response.CropResponse;
import tech.agrowerk.infrastructure.model.farming.Crop;

@Component
public class CropMapper {

    public Crop toEntity(CreateCropRequest request) {
        Crop crop = new Crop();
        crop.setName(request.name());
        crop.setScientificName(request.scientificName());
        crop.setGrowthCycleDays(request.growthCycleDays());
        crop.setCropCategory(request.cropCategory());
        return crop;
    }

    public CropResponse toResponse(Crop crop) {
        return new CropResponse(
                crop.getId(),
                crop.getName(),
                crop.getScientificName(),
                crop.getGrowthCycleDays(),
                crop.getCropCategory().name(),
                crop.getCreatedAt(),
                crop.getUpdatedAt()
        );
    }
}