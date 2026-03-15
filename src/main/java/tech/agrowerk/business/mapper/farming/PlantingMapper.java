package tech.agrowerk.business.mapper.farming;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreatePlantingRequest;
import tech.agrowerk.application.dto.response.farming.PlantingResponse;
import tech.agrowerk.infrastructure.model.farming.*;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.property.Property;

@Component
public class PlantingMapper {

    public Planting toEntity(CreatePlantingRequest request,
                             Property property,
                             Field field,
                             Season season,
                             Crop crop,
                             CropVariety cropVariety) {
        Planting planting = new Planting();
        planting.setProperty(property);
        planting.setField(field);
        planting.setSeason(season);
        planting.setCrop(crop);
        planting.setCropVariety(cropVariety);
        planting.setAreaHectares(request.areaHectares());
        planting.setPlantingDate(request.plantingDate());
        planting.setExpectedHarvestDate(request.expectedHarvestDate());
        planting.setPlantingStatus(PlantingStatus.IN_PROGRESS);
        return planting;
    }

    public PlantingResponse toResponse(Planting planting) {
        return new PlantingResponse(
                planting.getId(),
                planting.getProperty().getId(),
                planting.getProperty().getName(),
                planting.getField().getId(),
                planting.getField().getName(),
                planting.getSeason().getId(),
                planting.getSeason().getName(),
                planting.getCropVariety().getId(),
                planting.getCropVariety().getName(),
                planting.getCropVariety().getCrop().getName(),
                planting.getAreaHectares(),
                planting.getPlantingDate(),
                planting.getExpectedHarvestDate(),
                planting.getPlantingStatus().name(),
                planting.getCreatedAt(),
                planting.getUpdatedAt()
        );
    }
}