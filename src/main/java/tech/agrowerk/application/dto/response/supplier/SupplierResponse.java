package tech.agrowerk.application.dto.response.supplier;

import tech.agrowerk.application.dto.response.core.AddressResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String corporateReason,
        String fantasyName,
        String cnpj,
        String stateRegistration,
        String email,
        String telephone,
        String nameContact,
        AddressResponse address,
        String observations,
        Boolean isActive,
        Boolean acceptsBarterDeals,
        String barterTerms,
        BigDecimal averageRating,
        int totalRatings,
        List<SupplierSpecialtyResponse> specialties,
        Instant createdAt
) {}