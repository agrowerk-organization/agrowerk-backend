package tech.agrowerk.business.mapper.supplier;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.response.supplier.SupplierSpecialtyResponse;
import tech.agrowerk.infrastructure.model.supplier.SupplierSpecialtyLink;

import java.util.Collections;
import java.util.List;

@Component
public class SupplierSpecialtyMapper {
    public SupplierSpecialtyResponse toSpecialtyResponse(SupplierSpecialtyLink link) {
        return new SupplierSpecialtyResponse(
                link.getSpecialty().getId(),
                link.getSpecialty().getName(),
                link.getSpecialty().getDescription(),
                link.getIsActive()
        );
    }

    List<SupplierSpecialtyResponse> toSpecialtyResponses(List<SupplierSpecialtyLink> links) {
        if (links == null) return Collections.emptyList();
        return links.stream().map(this::toSpecialtyResponse).toList();
    }

}
