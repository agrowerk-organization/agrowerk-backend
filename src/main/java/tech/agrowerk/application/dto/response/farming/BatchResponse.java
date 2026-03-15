package tech.agrowerk.application.dto.response.farming;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BatchResponse(
        UUID id,
        String batchNumber,
        String invoiceNumber,
        UUID inputId,
        String inputName,
        UUID supplierId,
        String supplierName,
        UUID propertyId,
        String propertyName,
        BigDecimal initialQuantity,
        BigDecimal currentQuantity,
        LocalDate manufacturingDate,
        LocalDate expirationDate,
        LocalDate entryDate,
        BigDecimal unitPrice,
        BigDecimal totalValue,
        String status,
        String receiptStatus,
        LocalDateTime receivedAt,
        String notes,
        boolean nearExpiration,
        boolean expired,
        Instant createdAt,
        Instant updatedAt
) {
}
