package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreateHarvestForecastRequest;
import tech.agrowerk.application.dto.response.HarvestForecastResponse;
import tech.agrowerk.infrastructure.model.farming.HarvestForecast;
import tech.agrowerk.infrastructure.model.farming.Planting;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HarvestForecastMapper {

    public HarvestForecast toEntity(CreateHarvestForecastRequest request,
                                    Planting planting) {
        HarvestForecast forecast = new HarvestForecast();
        forecast.setPlanting(planting);
        forecast.setCrop(planting.getCropVariety().getCrop());
        forecast.setSeason(planting.getSeason());
        forecast.setProperty(planting.getProperty());
        forecast.setEstimatedQuantity(request.estimatedQuantity());
        forecast.setForecastDate(request.forecastDate());
        forecast.setConfidenceLevel(request.confidenceLevel());
        forecast.setPlantedArea(request.plantedArea() != null
                ? request.plantedArea()
                : planting.getAreaHectares());
        forecast.setNotes(request.notes());
        return forecast;
    }

    public HarvestForecastResponse toResponse(HarvestForecast forecast,
                                              BigDecimal actualQuantityKg) {
        BigDecimal accuracy = null;

        if (actualQuantityKg != null
                && forecast.getEstimatedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            accuracy = actualQuantityKg
                    .divide(forecast.getEstimatedQuantity(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Planting planting = forecast.getPlanting();
        return new HarvestForecastResponse(
                forecast.getId(),
                planting.getId(),
                planting.getCropVariety().getCrop().getName(),
                planting.getCropVariety().getName(),
                planting.getField().getName(),
                planting.getSeason().getName(),
                planting.getProperty().getName(),
                forecast.getEstimatedQuantity(),
                forecast.getForecastDate(),
                forecast.getConfidenceLevel().name(),
                forecast.getPlantedArea(),
                forecast.getNotes(),
                actualQuantityKg,
                accuracy,
                forecast.getCreatedAt(),
                forecast.getUpdatedAt()
        );
    }
}
