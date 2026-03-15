package tech.agrowerk.application.dto.response.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String name,
        String code,
        String warehouseType,
        BigDecimal capacityKg,
        BigDecimal currentOccupancyKg,
        BigDecimal availableCapacityKg,
        String location,
        String description,
        Boolean isActive,
        UUID propertyId,
        String propertyName,
        Instant createdAt,
        Instant updatedAt
) {}