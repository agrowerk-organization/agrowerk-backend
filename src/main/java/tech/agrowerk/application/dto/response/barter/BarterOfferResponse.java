package tech.agrowerk.application.dto.response.barter;

import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BarterOfferResponse(
        UUID id,
        String title,
        String description,
        UUID ownerId,
        String ownerName,
        UUID propertyId,
        String propertyName,
        OfferType offerType,
        UUID offeredCropId,
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
        String region,
        LocalDate expiresAt,
        Integer viewCount,
        LocalDateTime createdAt
) {}