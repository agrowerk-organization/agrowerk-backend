package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateBatchRequest(
        @NotBlank
        @Size(max = 50)
        String batchNumber,

        @Size(max = 50)
        String invoiceNumber,

        @NotNull
        UUID inputId,

        @NotNull
        UUID supplierId,

        @NotNull
        @Positive
        BigDecimal initialQuantity,

        @NotNull
        LocalDate manufacturingDate,

        @NotNull
        LocalDate expirationDate,

        @NotNull
        LocalDate entryDate,

        @NotNull
        @Positive
        BigDecimal unitPrice,

        String notes
) {}