package tech.agrowerk.application.dto.request.barter;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBarterOfferRequest(
        @Size(max = 255)
        String title,

        String description,

        String requestedDescription,

        @Positive
        BigDecimal requestedValue,

        @Size(max = 100)
        String region,

        @Future
        LocalDate expiresAt
) {
}
