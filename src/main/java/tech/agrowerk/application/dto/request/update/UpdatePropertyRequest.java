package tech.agrowerk.application.dto.request.update;

import tech.agrowerk.application.dto.request.create.AddressRequest;

import java.math.BigDecimal;

public record UpdatePropertyRequest(
        String name,
        String ruralRegistration,
        BigDecimal latitude,
        BigDecimal longitude,
        AddressRequest address,
        BigDecimal plantedArea,
        BigDecimal totalArea,
        String mainCrop,
        Boolean isActive
) {
}
