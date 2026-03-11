package tech.agrowerk.business.mapper;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.AddAddressRequest;
import tech.agrowerk.application.dto.request.create.AddFarmUnitRequest;
import tech.agrowerk.application.dto.request.create.CreatePropertyRequest;
import tech.agrowerk.application.dto.request.update.UpdateAddressRequest;
import tech.agrowerk.application.dto.request.update.UpdateFarmUnitRequest;
import tech.agrowerk.application.dto.response.FarmUnitResponse;
import tech.agrowerk.application.dto.response.PropertyResponse;
import tech.agrowerk.application.dto.response.AddressResponse;
import tech.agrowerk.infrastructure.model.core.Address;
import tech.agrowerk.infrastructure.model.property.FarmUnit;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.property.State;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


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
        property.setIsActive(true);

        if (request.units() != null && !request.units().isEmpty()) {
            List<FarmUnit> units = request.units().stream()
                    .map(unitRequest -> {
                        FarmUnit unit = new FarmUnit();
                        unit.setName(unitRequest.name());
                        unit.setArea(unitRequest.area());
                        unit.setAddress(toAddress(unitRequest.address()));
                        unit.setProperty(property);
                        return unit;
                    }).collect(Collectors.toList());
            property.setUnits(units);
        }

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
                property.getUnits() != null ?
                        property.getUnits().stream().map(this::toFarmUnitResponse).toList() :
                        Collections.emptyList(),
                property.getCreatedAt()
        );
    }

    public FarmUnit toFarmUnitEntity(AddFarmUnitRequest request, Property property) {
        if (request == null) return null;

        FarmUnit unit = new FarmUnit();
        unit.setName(request.name());
        unit.setArea(request.area());
        unit.setAddress(toAddress(request.address()));
        unit.setProperty(property);
        return unit;
    }

    public FarmUnit toFarmUnitEntity(UpdateFarmUnitRequest request, Property property) {
        if (request == null) return null;

        FarmUnit unit = new FarmUnit();
        unit.setName(request.name());
        unit.setArea(request.area());
        unit.setAddress(toAddress(request.address()));
        unit.setProperty(property);
        return unit;
    }

    private FarmUnitResponse toFarmUnitResponse(FarmUnit unit) {
        return new FarmUnitResponse(
                unit.getId(),
                unit.getName(),
                unit.getArea(),
                toAddressResponse(unit.getAddress())
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
}