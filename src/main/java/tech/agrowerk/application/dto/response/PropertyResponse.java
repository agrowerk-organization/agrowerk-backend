package tech.agrowerk.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PropertyResponse(
        UUID id,
        String name,
        String stateRegistration,
        String ruralRegistration,
        AddressResponse address,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal totalArea,
        BigDecimal plantedArea,
        String mainCrop,
        Boolean isActive,
        String stateName,
        List<FarmUnitResponse> units,
        Instant createdAt
) {}