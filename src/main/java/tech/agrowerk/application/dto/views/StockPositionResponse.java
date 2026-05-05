package tech.agrowerk.application.dto.views;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockPositionResponse(
        UUID stockId,
        UUID propertyId,
        String propertyName,
        String inputName,
        String categoryName,
        String stockType,
        BigDecimal currentQuantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,
        BigDecimal weightedAverageCost,
        BigDecimal totalValue,
        BigDecimal minimumStock,
        BigDecimal maximumStock,
        String stockAlert,
        String warehouseName,
        LocalDateTime lastEntryDate,
        LocalDateTime lastExitDate
) {}