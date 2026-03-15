package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.inventory.enums.WarehouseType;

import java.math.BigDecimal;

public record UpdateWarehouseRequest(
        @Size(max = 100)
        String name,

        @Size(max = 20)
        String code,

        WarehouseType warehouseType,

        @Positive
        BigDecimal capacityKg,

        @Size(max = 200)
        String location,

        String description
) {
}
