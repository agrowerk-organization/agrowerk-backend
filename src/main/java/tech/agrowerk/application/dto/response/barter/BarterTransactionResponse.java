package tech.agrowerk.application.dto.response.barter;

import tech.agrowerk.infrastructure.model.barter.enums.OfferType;
import tech.agrowerk.infrastructure.model.barter.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BarterTransactionResponse(
        UUID id,
        UUID offerId,
        String offerTitle,
        UUID offerorId,
        String offerorName,
        UUID acceptorId,
        String acceptorName,
        OfferType offerorGives,
        UUID offerorCropId,
        String offerorCropName,
        BigDecimal offerorCropQuantity,
        UUID offerorAssetId,
        String offerorAssetName,
        BigDecimal offerorAssetQuantity,
        OfferType acceptorGives,
        UUID acceptorCropId,
        String acceptorCropName,
        BigDecimal acceptorCropQuantity,
        TransactionStatus status,
        LocalDate offerorDeliveryDate,
        LocalDate acceptorDeliveryDate,
        String notes,
        UUID contractId,
        ContractSignatureStatus contractSignatureStatus,
        LocalDateTime createdAt
) {}