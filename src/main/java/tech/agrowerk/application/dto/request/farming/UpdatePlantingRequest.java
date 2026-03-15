package tech.agrowerk.application.dto.request.farming;

import java.time.LocalDate;
import java.util.UUID;

public record UpdatePlantingRequest(
        UUID cropVarietyId,
        LocalDate plantingDate,
        LocalDate expectedHarvestDate
) {
}
