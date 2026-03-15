package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InventoryAssetResponse(
        UUID id,
        String name,
        String description,
        String category,
        String condition,
        Integer quantity,
        BigDecimal referenceValue,
        String unit,
        Boolean available,
        Boolean approvedForBarter,
        UUID approvedById,
        String approvedByName,
        Instant approvedAt,
        String approvalNotes,
        String valuationMethod,
        BigDecimal agreedValue,
        String commodityReference,
        BigDecimal commodityQuantityEquivalent,
        UUID ownerId,
        String ownerName,
        UUID propertyId,
        String propertyName,
        List<String> photoUrls,
        Instant createdAt,
        Instant updatedAt
) {}