package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.views.ActivePlantingResponse;
import tech.agrowerk.application.dto.views.FieldProductivityResponse;
import tech.agrowerk.application.dto.views.SeasonDashboardResponse;
import tech.agrowerk.infrastructure.model.farming.views.ActivePlantingView;
import tech.agrowerk.infrastructure.model.farming.views.FieldProductivityView;
import tech.agrowerk.infrastructure.model.farming.views.SeasonDashboardView;

@Component
public class FarmingViewMapper {

    public ActivePlantingResponse toActivePlantingResponse(ActivePlantingView view) {
        return new ActivePlantingResponse(
                view.getPlantingId(),
                view.getPropertyName(),
                view.getCropName(),
                view.getSeasonName(),
                view.getFieldName(),
                view.getAreaHectares(),
                view.getPlantingDate(),
                view.getExpectedHarvestDate(),
                view.getPlantingStatus()
        );
    }

    public FieldProductivityResponse toFieldProductivityResponse(FieldProductivityView view) {
        return new FieldProductivityResponse(
                view.getFieldId(),
                view.getFieldName(),
                view.getPropertyName(),
                view.getAvgProductivity(),
                view.getTotalProducedKg(),
                view.getTotalHarvests()
        );
    }

    public SeasonDashboardResponse toSeasonDashboardResponse(SeasonDashboardView view) {
        return new SeasonDashboardResponse(
                view.getSeasonId(),
                view.getSeasonName(),
                view.getPropertyId(),
                view.getPropertyName(),
                view.getCropName(),
                view.getTotalPlantings(),
                view.getTotalArea(),
                view.getTotalProducedKg(),
                view.getAvgProductivity(),
                "Data may be up to 1 hour old"
        );
    }
}