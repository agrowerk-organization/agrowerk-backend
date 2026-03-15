package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InputResponse(
        UUID id,
        String name,
        String internalCode,
        String manufacturerCode,
        String description,
        String unitOfMeasure,
        String activeIngredient,
        String formulation,
        String concentration,
        String mapaRegistration,
        String toxicologicalClass,
        Integer gracePeriod,
        BigDecimal minimumStock,
        BigDecimal maximumStock,
        BigDecimal averagePurchasePrice,
        BigDecimal lastPurchasePrice,
        Boolean active,
        Boolean controlled,
        Boolean globalVisible,
        UUID categoryId,
        String categoryName,
        UUID supplierId,
        String supplierName,
        Instant createdAt,
        Instant updatedAt
) {}