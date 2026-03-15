package tech.agrowerk.application.dto.request.property;

import tech.agrowerk.application.dto.request.core.UpdateAddressRequest;

import java.math.BigDecimal;

public record UpdateFarmUnitRequest(
        String name,
        BigDecimal area,
        UpdateAddressRequest address
) {
}
