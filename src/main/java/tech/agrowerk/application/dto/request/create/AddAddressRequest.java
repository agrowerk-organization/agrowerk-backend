package tech.agrowerk.application.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddAddressRequest(
        @NotNull boolean rural,
        @NotBlank String code,
        @NotBlank String municipality,
        String locationName,
        String street,
        Integer number,
        String neighborhood,
        String landmark
) {}