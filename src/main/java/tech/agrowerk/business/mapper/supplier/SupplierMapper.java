package tech.agrowerk.business.mapper.supplier;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.core.AddAddressRequest;
import tech.agrowerk.application.dto.request.supplier.CreateSupplierRequest;
import tech.agrowerk.application.dto.response.core.AddressResponse;
import tech.agrowerk.application.dto.response.supplier.*;
import tech.agrowerk.infrastructure.model.core.Address;
import tech.agrowerk.infrastructure.model.supplier.*;

import java.util.Collections;
import java.util.List;

@Component
public class SupplierMapper {

    private final SupplierSpecialtyMapper supplierSpecialtyMapper;

    public SupplierMapper(SupplierSpecialtyMapper supplierSpecialtyMapper) {
        this.supplierSpecialtyMapper = supplierSpecialtyMapper;
    }

    public Supplier toEntity(CreateSupplierRequest request) {
        Supplier supplier = new Supplier();
        supplier.setCorporateReason(request.corporateReason());
        supplier.setFantasyName(request.fantasyName());
        supplier.setCnpj(request.cnpj());
        supplier.setStateRegistration(request.stateRegistration());
        supplier.setEmail(request.email());
        supplier.setTelephone(request.telephone());
        supplier.setNameContact(request.nameContact());
        supplier.setAddress(toAddress(request.address()));
        supplier.setObservations(request.observations());
        supplier.setIsActive(true);
        supplier.setAcceptsBarterDeals(request.acceptsBarterDeals() != null && request.acceptsBarterDeals());
        supplier.setBarterTerms(request.barterTerms());

        return supplier;
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getCorporateReason(),
                supplier.getFantasyName(),
                supplier.getCnpj(),
                supplier.getStateRegistration(),
                supplier.getEmail(),
                supplier.getTelephone(),
                supplier.getNameContact(),
                toAddressResponse(supplier.getAddress()),
                supplier.getObservations(),
                supplier.getIsActive(),
                supplier.getAcceptsBarterDeals(),
                supplier.getBarterTerms(),
                supplier.getAverageRating(),
                supplier.getRatings() != null ? supplier.getRatings().size() : 0,
                supplierSpecialtyMapper.toSpecialtyResponses(supplier.getSpecialties()),
                supplier.getCreatedAt()
        );
    }

    public Address toAddress(AddAddressRequest request) {
        if (request == null) return null;

        Address address = new Address();
        address.setRural(request.rural());
        address.setCode(request.code());
        address.setMunicipality(request.municipality());
        address.setLocationName(request.locationName());
        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setNeighborhood(request.neighborhood());
        address.setLandmark(request.landmark());

        return address;
    }

    private AddressResponse toAddressResponse(Address address) {
        if (address == null) return null;
        return new AddressResponse(
                address.isRural(),
                address.getCode(),
                address.getMunicipality(),
                address.getLocationName(),
                address.getStreet(),
                address.getNumber(),
                address.getNeighborhood(),
                address.getLandmark()
        );
    }
}

