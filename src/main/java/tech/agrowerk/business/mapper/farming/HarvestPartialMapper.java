package tech.agrowerk.business.mapper.farming;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.farming.CreateHarvestPartialRequest;
import tech.agrowerk.application.dto.response.farming.HarvestPartialResponse;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Harvest;
import tech.agrowerk.infrastructure.model.farming.HarvestPartial;

import java.math.BigDecimal;

@Component
public class HarvestPartialMapper {

    public HarvestPartial toPartialEntity(CreateHarvestPartialRequest request,
                                          Harvest harvest,
                                          User responsibleUser) {
        HarvestPartial partial = new HarvestPartial();
        partial.setHarvest(harvest);
        partial.setPartialDate(request.partialDate());
        partial.setQuantityKg(request.quantityKg());
        partial.setQualityGrade(request.qualityGrade());
        partial.setNotes(request.notes());
        partial.setResponsibleUser(responsibleUser);
        return partial;
    }

    public HarvestPartialResponse toPartialResponse(HarvestPartial partial, BigDecimal currentQuantityKg) {
        return new HarvestPartialResponse(
                partial.getId(),
                partial.getHarvest().getId(),
                partial.getPartialDate(),
                partial.getQuantityKg(),
                partial.getQualityGrade(),
                partial.getNotes(),
                partial.getResponsibleUser().getId(),
                partial.getResponsibleUser().getName(),
                currentQuantityKg,
                partial.getCreatedAt()
        );
    }
}
