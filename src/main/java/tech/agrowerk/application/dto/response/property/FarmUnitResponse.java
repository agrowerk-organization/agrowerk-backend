package tech.agrowerk.application.dto.response.property;

import tech.agrowerk.application.dto.response.core.AddressResponse;

import java.math.BigDecimal;
import java.util.UUID;

public record FarmUnitResponse(
        UUID id,
        String name,
        BigDecimal area,
        AddressResponse response
) {}