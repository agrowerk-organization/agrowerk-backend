package tech.agrowerk.application.dto.response.core;

public record AddressResponse(
        boolean rural,
        String code,
        String municipality,
        String locationName,
        String street,
        Integer number,
        String neighborhood,
        String landmark
) {}