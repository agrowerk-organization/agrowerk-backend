package tech.agrowerk.application.dto.response.farming;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponse(
        UUID id,
        UUID plantingId,
        String cropName,
        String fieldName,
        String propertyName,
        String agronomistName,
        String agronomistCrea,
        LocalDate issuedAt,
        LocalDate validUntil,
        String documentUrl,
        Boolean active,
        boolean expired,
        List<PrescriptionItemResponse> items,
        Instant createdAt
) {}
