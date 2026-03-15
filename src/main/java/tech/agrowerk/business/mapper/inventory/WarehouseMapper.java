package tech.agrowerk.business.mapper.inventory;

import org.springframework.stereotype.Component;
import tech.agrowerk.application.dto.request.inventory.CreateWarehouseRequest;
import tech.agrowerk.application.dto.response.inventory.WarehouseResponse;
import tech.agrowerk.infrastructure.model.inventory.Warehouse;
import tech.agrowerk.infrastructure.model.property.Property;

import java.math.BigDecimal;

@Component
public class WarehouseMapper {

    public Warehouse toEntity(CreateWarehouseRequest request,
                              Property property) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.name());
        warehouse.setCode(request.code());
        warehouse.setWarehouseType(request.warehouseType());
        warehouse.setCapacityKg(request.capacityKg());
        warehouse.setCurrentOccupancyKg(BigDecimal.ZERO);
        warehouse.setLocation(request.location());
        warehouse.setDescription(request.description());
        warehouse.setProperty(property);
        warehouse.setIsActive(true);
        return warehouse;
    }

    public WarehouseResponse toResponse(Warehouse warehouse) {
        BigDecimal available = warehouse.getCapacityKg() != null
                ? warehouse.getCapacityKg().subtract(
                warehouse.getCurrentOccupancyKg() != null
                        ? warehouse.getCurrentOccupancyKg()
                        : BigDecimal.ZERO)
                : null;

        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getCode(),
                warehouse.getWarehouseType().name(),
                warehouse.getCapacityKg(),
                warehouse.getCurrentOccupancyKg(),
                available,
                warehouse.getLocation(),
                warehouse.getDescription(),
                warehouse.getIsActive(),
                warehouse.getProperty().getId(),
                warehouse.getProperty().getName(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }
}