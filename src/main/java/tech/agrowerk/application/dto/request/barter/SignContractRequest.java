package tech.agrowerk.application.dto.request.barter;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SignContractRequest(
        @NotNull
        UUID contractId,

        @AssertTrue(message = "You must accept the contract terms to sign")
        boolean accepted
) {}