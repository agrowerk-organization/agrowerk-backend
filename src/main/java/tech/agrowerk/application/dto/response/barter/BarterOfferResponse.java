package tech.agrowerk.application.dto.response.barter;

import tech.agrowerk.application.dto.market.CommodityResolution;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BarterOfferResponse(
        UUID id,
        String title,
        String description,
        UUID ownerId,
        String ownerName,
        UUID propertyId,
        String propertyName,
        String propertyLocal,
        OfferType offerType,
        UUID harvestForecastId,
        String offeredCropName,
        BigDecimal offeredCropQuantity,
        LocalDate estimatedHarvestDate,
        UUID offeredAssetId,
        String offeredAssetName,
        BigDecimal offeredAssetQuantity,
        OfferType requestedType,
        String requestedDescription,
        BigDecimal requestedValue,
        OfferStatus status,
        LocalDate expiresAt,
        Integer viewCount,
        List<BarterOfferItemResponse> requestedItems,
        BigDecimal suggestedQuantity,
        BigDecimal referencePrice,
        LocalDate referencePriceDate,
        LocalDateTime createdAt
) {}