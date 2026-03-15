package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockResponse(
        UUID id,
        UUID propertyId,
        String propertyName,
        UUID inputId,
        String inputName,
        String inputCategory,
        String stockType,
        BigDecimal currentQuantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,
        BigDecimal totalValue,
        BigDecimal weightedAverageCost,
        String stockAlert,
        UUID warehouseId,
        String warehouseName,
        LocalDateTime lastEntryDate,
        LocalDateTime lastExitDate,
        Instant createdAt,
        Instant updatedAt
) {
}
