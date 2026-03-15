package tech.agrowerk.application.dto.request.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.agrowerk.infrastructure.model.inventory.enums.WarehouseType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWarehouseRequest(
        @NotNull
        UUID propertyId,

        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 20)
        String code,

        @NotNull
        WarehouseType warehouseType,

        @Positive
        BigDecimal capacityKg,

        @Size(max = 200)
        String location,

        String description
) {}