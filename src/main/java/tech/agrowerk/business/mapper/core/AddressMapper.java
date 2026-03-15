package tech.agrowerk.business.mapper.core;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.core.AddAddressRequest;
import tech.agrowerk.application.dto.request.core.UpdateAddressRequest;
import tech.agrowerk.application.dto.response.core.AddressResponse;
import tech.agrowerk.infrastructure.model.core.Address;

@Component
public class AddressMapper {

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

    public Address toAddress(UpdateAddressRequest request) {
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

    public AddressResponse toAddressResponse(Address address) {
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