package tech.agrowerk.application.dto.response.supplier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SupplierRatingResponse(
        UUID id,
        UUID supplierId,
        String ratedByName,
        BigDecimal rating,
        String comment,
        Instant createdAt
) {}