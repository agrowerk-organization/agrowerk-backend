package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
        UUID movementId,
        String movementType,
        BigDecimal quantity,
        BigDecimal unitValue,
        BigDecimal totalValue,
        LocalDateTime movementDate,
        UUID propertyId,
        String propertyName,
        String inputName,
        String userName,
        String batchNumber,
        String notes,
        Boolean reversed,
        UUID reversedMovementId
) {}