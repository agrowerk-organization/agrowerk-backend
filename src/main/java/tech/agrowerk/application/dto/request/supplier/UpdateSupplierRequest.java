package tech.agrowerk.application.dto.request.supplier;

import jakarta.validation.constraints.*;
import tech.agrowerk.application.dto.request.core.AddAddressRequest;

public record UpdateSupplierRequest(
        @Size(max = 255)
        String corporateReason,

        @Size(max = 255)
        String fantasyName,

        @Size(max = 255)
        String stateRegistration,

        @Email @Size(max = 255)
        String email,

        @Pattern(regexp = "^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$")
        String telephone,

        @Size(max = 255)
        String nameContact,

        AddAddressRequest address,

        String observations,

        Boolean acceptsBarterDeals,

        String barterTerms
) {}