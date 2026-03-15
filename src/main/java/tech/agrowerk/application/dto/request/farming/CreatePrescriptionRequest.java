package tech.agrowerk.application.dto.request.farming;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePrescriptionRequest(
        @NotNull
        UUID plantingId,

        @NotBlank
        String agronomistName,

        @NotBlank
        @Size(max = 20)
        String agronomistCrea,

        @NotNull
        LocalDate issuedAt,

        @NotNull
        LocalDate validUntil,

        @NotNull @Size(min = 1)
        List<CreatePrescriptionItemRequest> items
) {}