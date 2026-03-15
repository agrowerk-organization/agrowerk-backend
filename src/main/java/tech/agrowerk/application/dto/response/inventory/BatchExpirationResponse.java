package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BatchExpirationResponse(
        UUID batchId,
        String batchNumber,
        String inputName,
        String categoryName,
        UUID propertyId,
        String propertyName,
        String supplierName,
        BigDecimal currentQuantity,
        LocalDate expirationDate,
        BigDecimal unitPrice,
        BigDecimal currentValue,
        Integer daysUntilExpiration,
        String expirationStatus
) {}