package tech.agrowerk.application.dto.views;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ActivePlantingResponse(
        UUID plantingId,
        String propertyName,
        String cropName,
        String seasonName,
        String fieldName,
        BigDecimal areaHectares,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate,
        String plantingStatus
) {}