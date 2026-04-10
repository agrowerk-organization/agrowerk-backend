package tech.agrowerk.application.dto.response.barter;

import tech.agrowerk.infrastructure.model.barter.enums.CommitmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CropCommitmentResponse(
        UUID id,
        UUID transactionId,
        UUID farmerId,
        String farmerName,
        UUID cropId,
        String cropName,
        BigDecimal committedQuantity,
        BigDecimal deliveredQuantity,
        BigDecimal pendingQuantity,
        BigDecimal progressPercent,
        LocalDate expectedDeliveryDate,
        LocalDate actualDeliveryDate,
        CommitmentStatus status,
        String notes
) {
}
