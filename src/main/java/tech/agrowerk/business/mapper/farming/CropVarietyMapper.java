package tech.agrowerk.business.mapper.farming;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreateCropVarietyRequest;
import tech.agrowerk.application.dto.response.farming.CropVarietyResponse;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.farming.CropVariety;
import tech.agrowerk.infrastructure.model.farming.enums.BrazilRegion;

@Component
public class CropVarietyMapper {

    public CropVariety toEntity(CreateCropVarietyRequest request, Crop crop, User createdBy) {
        CropVariety variety = new CropVariety();
        variety.setName(request.name());
        variety.setDescription(request.description());
        variety.setRegion(BrazilRegion.valueOf(request.region()));
        variety.setCrop(crop);
        variety.setCreatedBy(createdBy);
        return variety;
    }

    public CropVarietyResponse toResponse(CropVariety variety) {
        return new CropVarietyResponse(
                variety.getId(),
                variety.getName(),
                variety.getDescription(),
                variety.getRegion().name(),
                variety.getCrop().getId(),
                variety.getCrop().getName(),
                variety.getCreatedBy().getId(),
                variety.getCreatedBy().getName(),
                variety.getCreatedAt(),
                variety.getUpdatedAt()
        );
    }
}
