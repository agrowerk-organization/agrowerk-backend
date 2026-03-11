package tech.agrowerk.application.dto.request.update;

public record UpdateAddressRequest(
        boolean rural,
        String code,
        String municipality,
        String locationName,
        Integer number,
        String street,
        String neighborhood,
        String landmark
) {}