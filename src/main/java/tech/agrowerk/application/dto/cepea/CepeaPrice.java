package tech.agrowerk.application.dto.cepea;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CepeaPrice(
        String commodity,
        BigDecimal price,
        String unit,
        String region,
        LocalDate referenceDate
) {
}
