package tech.agrowerk.business.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.AddressRequest;
import tech.agrowerk.application.dto.request.create.CreatePropertyRequest;
import tech.agrowerk.application.dto.response.PropertyResponse;
import tech.agrowerk.application.dto.response.AddressResponse;
import tech.agrowerk.infrastructure.model.core.Address;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.property.State;


@Mapper(componentModel = "spring")
@Component
public class PropertyMapper {

    public Property toEntity(CreatePropertyRequest request, State state) {
        Property property = new Property();

        property.setName(request.name());
        property.setStateRegistration(request.stateRegistration());
        property.setRuralRegistration(request.ruralRegistration());
        property.setLatitude(request.latitude());
        property.setLongitude(request.longitude());
        property.setTotalArea(request.totalArea());
        property.setPlantedArea(request.plantedArea());
        property.setMainCrop(request.mainCrop());
        property.setState(state);
        property.setAddress(toAddress(request.address()));

        return property;
    }

    public PropertyResponse toResponse(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getStateRegistration(),
                property.getRuralRegistration(),
                toAddressResponse(property.getAddress()),
                property.getLatitude(),
                property.getLongitude(),
                property.getTotalArea(),
                property.getPlantedArea(),
                property.getMainCrop(),
                property.getIsActive(),
                property.getState() != null ? property.getState().getName() : null,
                property.getCreatedAt()
        );
    }

    public Address toAddress(AddressRequest request) {
        Address address = new Address();
        address.setMunicipality(request.municipality());
        address.setCode(request.code());
        address.setNumber(request.number());
        address.setStreet(request.street());
        address.setNeighborhood(request.neighborhood());
        return address;
    }

    private AddressResponse toAddressResponse(Address address) {
        if (address == null) return null;
        return new AddressResponse(
                address.getMunicipality(),
                address.getCode(),
                address.getNumber(),
                address.getStreet(),
                address.getNeighborhood()
        );
    }
}