package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.util.UUID;

public record PropertyUpdatedEvent(
        UUID propertyId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
