package tech.agrowerk.application.dto.response;

import tech.agrowerk.infrastructure.model.core.Address;

import java.math.BigDecimal;
import java.util.UUID;

public record FarmUnitResponse(
        UUID id,
        String name,
        BigDecimal area,
        AddressResponse response
) {}