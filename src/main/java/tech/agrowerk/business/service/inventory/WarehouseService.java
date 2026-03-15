package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.request.inventory.CreateWarehouseRequest;
import tech.agrowerk.application.dto.request.inventory.UpdateWarehouseRequest;
import tech.agrowerk.application.dto.response.inventory.WarehouseResponse;
import tech.agrowerk.business.mapper.inventory.WarehouseMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.exception.local.EntityAlreadyExistsException;
import tech.agrowerk.infrastructure.exception.local.EntityNotFoundException;
import tech.agrowerk.infrastructure.model.inventory.Warehouse;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.repository.inventory.WarehouseRepository;
import tech.agrowerk.infrastructure.repository.property.PropertyRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final PropertyRepository propertyRepository;
    private final WarehouseMapper warehouseMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public WarehouseService(WarehouseRepository warehouseRepository, PropertyRepository propertyRepository, WarehouseMapper warehouseMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.warehouseRepository = warehouseRepository;
        this.propertyRepository = propertyRepository;
        this.warehouseMapper = warehouseMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional
    public WarehouseResponse createWarehouse(CreateWarehouseRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        ownershipValidator.validateEditPermission(request.propertyId(), auth.id());

        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

        if (warehouseRepository.existsByNameIgnoreCaseAndProperty_Id(request.name(), request.propertyId())) {
            throw new EntityAlreadyExistsException("Warehouse name already exists");
        }

        if (request.code() != null && warehouseRepository.existsByCodeAndProperty_Id(request.code(), request.propertyId())) {
            throw new EntityAlreadyExistsException("Warehouse code already exists");
        }

        Warehouse warehouse = warehouseMapper.toEntity(request, property);
        Warehouse saved = warehouseRepository.save(warehouse);

        log.info("Warehouse created id={} property={}", saved.getId(), request.propertyId());

        return warehouseMapper.toResponse(saved);
    }

    @Transactional
    public WarehouseResponse updateWarehouse(UUID warehouseId, UpdateWarehouseRequest request) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        ownershipValidator.validateEditPermission(warehouse.getProperty().getId(), auth.id());

        boolean hasChanges = false;

        if (request.name() != null && !request.name().isBlank()) {
            if (warehouseRepository.existsByNameIgnoreCaseAndProperty_Id(
                    request.name(), warehouse.getProperty().getId())) {
                throw new EntityAlreadyExistsException(
                        "Warehouse name already exists"
                );
            }
            warehouse.setName(request.name());
            hasChanges = true;
        }

        if (request.warehouseType() != null) {
            warehouse.setWarehouseType(request.warehouseType());
            hasChanges = true;
        }

        if (request.capacityKg() != null) {
            if (warehouse.getCurrentOccupancyKg() != null &&
                    request.capacityKg().compareTo(
                            warehouse.getCurrentOccupancyKg()) < 0) {
                throw new IllegalArgumentException(
                        String.format(
                                "New capacity %.2fkg is less than " +
                                        "current occupancy %.2fkg",
                                request.capacityKg(),
                                warehouse.getCurrentOccupancyKg())
                );
            }
            warehouse.setCapacityKg(request.capacityKg());
            hasChanges = true;
        }

        if (request.location() != null) {
            warehouse.setLocation(request.location());
            hasChanges = true;
        }

        if (request.description() != null) {
            warehouse.setDescription(request.description());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.warn("No changes for warehouse id={}", warehouseId);
        }

        log.info("Warehouse updated id={}", warehouseId);
        return warehouseMapper.toResponse(warehouse);
    }

    @Transactional
    public void deactivateWarehouse(UUID warehouseId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        ownershipValidator.validateEditPermission(warehouse.getProperty().getId(), auth.id());

        if (warehouseRepository.hasActiveStock(warehouseId)) {
            throw new IllegalArgumentException("Cannot deactivate warehouse with active stock");
        }

        warehouse.setIsActive(false);
        log.info("Warehouse deactivated id={}", warehouseId);
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findByProperty(UUID propertyId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();
        ownershipValidator.validateOwnership(propertyId, auth.id());

        return warehouseRepository.findByProperty_IdAndIsActiveTrue(propertyId)
                .stream()
                .map(warehouseMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse findById(UUID warehouseId) {
        AuthenticatedUser auth = authUtil.getAuthenticatedUser();

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        ownershipValidator.validateOwnership(warehouse.getProperty().getId(), auth.id());

        return warehouseMapper.toResponse(warehouse);
    }
}
