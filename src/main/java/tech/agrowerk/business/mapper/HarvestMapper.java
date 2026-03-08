package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreateHarvestRequest;
import tech.agrowerk.application.dto.response.HarvestResponse;
import tech.agrowerk.infrastructure.model.farming.Harvest;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.inventory.Stock;

import java.math.BigDecimal;

@Component
public class HarvestMapper {

    public Harvest toEntity(CreateHarvestRequest request, Planting planting, Stock stock) {
        Harvest harvest = new Harvest();
        harvest.setPlanting(planting);
        harvest.setHarvestDate(request.harvestDate());
        harvest.setQuantityKg(request.quantityKg());
        harvest.setQualityGrade(request.qualityGrade());
        harvest.setStock(stock);
        return harvest;
    }

    public HarvestResponse toResponse(Harvest harvest, BigDecimal totalPlantingCost, BigDecimal totalQuantityKg) {
        Planting planting = harvest.getPlanting();
        return new HarvestResponse(
                harvest.getId(),
                planting.getId(),
                planting.getCropVariety().getCrop().getName(),
                planting.getCropVariety().getName(),
                planting.getField().getName(),
                planting.getProperty().getName(),
                planting.getSeason().getName(),
                harvest.getHarvestDate(),
                harvest.getQuantityKg(),
                harvest.getQualityGrade(),
                totalPlantingCost,
                totalQuantityKg,
                harvest.getStock().getWeightedAverageCost(),
                harvest.getCreatedAt()
        );
    }
}