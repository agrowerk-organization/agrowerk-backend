package tech.agrowerk.application.dto.request.barter;

import jakarta.validation.constraints.*;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBarterOfferRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        @NotNull
        UUID propertyId,

        @NotNull
        OfferType offerType,

        UUID offeredCropId,

        @Positive
        BigDecimal offeredCropQuantity,

        LocalDate estimatedHarvestDate,

        UUID offeredAssetId,

        @Positive
        BigDecimal offeredAssetQuantity,

        @NotNull
        OfferType requestedType,

        String requestedDescription,

        @Positive
        BigDecimal requestedValue,

        @Size(max = 100)
        String region,

        @NotNull
        @Future
        LocalDate expiresAt
) {
}
