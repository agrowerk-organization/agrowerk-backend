package tech.agrowerk.application.dto.request.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tech.agrowerk.application.dto.request.core.AddAddressRequest;

import java.math.BigDecimal;

public record AddFarmUnitRequest(
        @NotBlank String name,
        @NotNull BigDecimal area,
        @NotNull AddAddressRequest address
) {}
