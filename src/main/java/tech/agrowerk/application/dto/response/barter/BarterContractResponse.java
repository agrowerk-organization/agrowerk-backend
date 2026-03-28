package tech.agrowerk.application.dto.response.barter;

import tech.agrowerk.infrastructure.model.barter.enums.ContractStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BarterContractResponse(
        UUID id,
        UUID transactionId,
        String contractNumber,
        LocalDate startDate,
        LocalDate endDate,
        ContractStatus contractStatus,
        String termsAndConditions,
        boolean offerorSigned,
        Instant offerorSignedAt,
        boolean acceptorSigned,
        Instant acceptorSignedAt,
        Instant createdAt
) {}