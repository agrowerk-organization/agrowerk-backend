package tech.agrowerk.application.dto.response;

public record AddressResponse(
        String municipality,
        String code,
        int number,
        String street,
        String neighborhood
) {}