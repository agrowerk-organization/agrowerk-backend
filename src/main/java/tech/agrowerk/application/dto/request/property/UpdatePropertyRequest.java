package tech.agrowerk.application.dto.request.property;

import tech.agrowerk.application.dto.request.core.AddAddressRequest;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePropertyRequest(
        String name,
        String ruralRegistration,
        BigDecimal latitude,
        BigDecimal longitude,
        AddAddressRequest address,
        BigDecimal plantedArea,
        BigDecimal totalArea,
        String mainCrop,
        Boolean isActive,
        List<UpdateFarmUnitRequest> units
) {
}
