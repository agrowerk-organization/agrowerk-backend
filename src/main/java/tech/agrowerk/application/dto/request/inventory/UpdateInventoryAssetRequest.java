package tech.agrowerk.application.dto.request.inventory;


import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetCondition;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetValuationMethod;

import java.math.BigDecimal;

public record UpdateInventoryAssetRequest(
        String description,

        AssetCondition condition,

        @Positive Integer quantity,

        @Positive
        BigDecimal referenceValue,

        String unit,

        AssetValuationMethod valuationMethod,

        BigDecimal agreedValue,

        String commodityReference,

        BigDecimal commodityQuantityEquivalent
) {}