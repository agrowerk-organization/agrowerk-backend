package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record StockTransferEvent(
        UUID sourceStockId,
        UUID targetStockId,
        UUID sourcePropertyId,
        UUID targetPropertyId,
        UUID inputId,
        UUID userId,
        BigDecimal quantity,
        BigDecimal weightedAverageCost,
        String justification
) {
}
