package tech.agrowerk.business.mapper;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.create.CreateFieldRequest;
import tech.agrowerk.application.dto.response.FieldResponse;
import tech.agrowerk.infrastructure.model.farming.Field;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.valueobject.Geolocation;

@Component
public class FieldMapper {

    public Field toEntity(CreateFieldRequest request, Property property) {
        Field field = new Field();
        field.setName(request.name());
        field.setCode(request.code());
        field.setAreaHectares(request.areaHectares());
        field.setDescription(request.description());
        field.setSoilType(request.soilType());
        field.setFieldStatus(request.fieldStatus());
        field.setSlopePercentage(request.slopePercentage());
        field.setNotes(request.notes());
        field.setProperty(property);

        if (request.latitude() != null && request.longitude() != null) {
            field.setGeolocation(new Geolocation(request.latitude(), request.longitude()));
        }

        return field;
    }

    public FieldResponse toResponse(Field field) {
        return new FieldResponse(
                field.getId(),
                field.getName(),
                field.getCode(),
                field.getAreaHectares(),
                field.getDescription(),
                field.getSoilType().name(),
                field.getFieldStatus().name(),
                field.getSlopePercentage(),
                field.getNotes(),
                field.getGeolocation() != null ? field.getGeolocation().getLatitude() : null,
                field.getGeolocation() != null ? field.getGeolocation().getLongitude() : null,
                field.getProperty().getId(),
                field.getProperty().getName(),
                field.getCreatedAt(),
                field.getUpdatedAt()
        );
    }
}