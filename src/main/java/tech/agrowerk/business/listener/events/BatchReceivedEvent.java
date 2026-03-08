package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record BatchReceivedEvent(
        UUID batchId,
        UUID inputId,
        UUID propertyId,
        UUID receivedBy,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalValue
) {
}
