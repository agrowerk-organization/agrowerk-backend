package tech.agrowerk.application.dto.request.create;

public record AddressRequest(
        String municipality,
        String code,
        int number,
        String street,
        String neighborhood
) {}