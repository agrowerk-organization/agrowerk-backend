package tech.agrowerk.application.dto.request.update;

import java.math.BigDecimal;

public record UpdateFarmUnitRequest(
        String name,
        BigDecimal area,
        UpdateAddressRequest address
) {
}
