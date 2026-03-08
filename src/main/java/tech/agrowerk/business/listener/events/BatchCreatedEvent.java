package tech.agrowerk.business.listener.events;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BatchCreatedEvent(
        UUID batchId,
        UUID inputId,
        UUID supplierId,
        BigDecimal initialQuantity,
        BigDecimal unitPrice,
        LocalDate expirationDate
) {}