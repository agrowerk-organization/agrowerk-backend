package tech.agrowerk.application.dto.response.supplier;

import java.util.UUID;

public record SupplierSpecialtyResponse(
        UUID id,
        String name,
        String description,
        Boolean isActive
) {}