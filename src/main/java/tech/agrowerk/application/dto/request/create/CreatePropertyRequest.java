package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePropertyRequest(
        @NotBlank String name,
        @NotBlank String stateRegistration,
        String ruralRegistration,
        @NotNull AddressRequest address,
        BigDecimal latitude,
        BigDecimal longitude,
        @NotNull BigDecimal totalArea,
        BigDecimal plantedArea,
        String mainCrop,
        @NotNull UUID stateId
) {}
