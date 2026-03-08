package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreateAgriculturalPracticeRequest;
import tech.agrowerk.application.dto.response.AgriculturalPracticeResponse;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.AgriculturalPractice;
import tech.agrowerk.infrastructure.model.farming.Planting;

@Component
public class AgriculturalPracticeMapper {

    public AgriculturalPractice toEntity(CreateAgriculturalPracticeRequest request,
                                         Planting planting,
                                         User responsibleUser) {
        AgriculturalPractice practice = new AgriculturalPractice();
        practice.setPlanting(planting);
        practice.setPractipeType(request.practipeType());
        practice.setApplicationDate(request.applicationDate());
        practice.setProductUsed(request.productUsed());
        practice.setQuantityUsed(request.quantityUsed());
        practice.setUnitOfMeasure(request.unitOfMeasure());
        practice.setCostAmount(request.costAmount());
        practice.setObservations(request.observations());
        practice.setResponsibleUser(responsibleUser);
        return practice;
    }

    public AgriculturalPracticeResponse toResponse(AgriculturalPractice practice) {
        return new AgriculturalPracticeResponse(
                practice.getId(),
                practice.getPlanting().getId(),
                practice.getPlanting().getCropVariety().getCrop().getName(),
                practice.getPlanting().getField().getName(),
                practice.getPractipeType().name(),
                practice.getApplicationDate(),
                practice.getProductUsed(),
                practice.getQuantityUsed(),
                practice.getUnitOfMeasure() != null
                        ? practice.getUnitOfMeasure().name() : null,
                practice.getCostAmount(),
                practice.getResponsibleUser().getId(),
                practice.getResponsibleUser().getName(),
                practice.getObservations(),
                practice.getCreatedAt()
        );
    }
}