package tech.agrowerk.application.dto.request.barter;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProposeTransactionRequest(
        @NotNull
        UUID offerId,

        @NotNull
        OfferType offerorGives,

        UUID offerorCropId,

        @Positive
        BigDecimal offerorCropQuantity,

        UUID offerorAssetId,

        @Positive
        BigDecimal offerorAssetQuantity,

        @NotNull
        @Future
        LocalDate offerorDeliveryDate,

        @NotNull
        @Future
        LocalDate acceptorDeliveryDate,

        String notes
) {
}
