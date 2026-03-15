package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetCategory;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetCondition;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetValuationMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventoryAssetRequest(
        @NotBlank
        String name,

        String description,

        @NotNull
        AssetCategory category,

        @NotNull
        AssetCondition condition,

        @NotNull
        @Positive
        Integer quantity,

        @NotNull
        @Positive
        BigDecimal referenceValue,

        String unit,

        UUID propertyId,

        @NotNull
        AssetValuationMethod valuationMethod,

        BigDecimal agreedValue,

        String commodityReference,

        BigDecimal commodityQuantityEquivalent
) {}
