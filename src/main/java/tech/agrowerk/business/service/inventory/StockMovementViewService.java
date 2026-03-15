package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.response.inventory.StockMovementResponse;
import tech.agrowerk.business.mapper.inventory.StockMovementViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.inventory.StockMovementViewRepository;

import java.util.UUID;

@Service
@Slf4j
public class StockMovementViewService {

    private final StockMovementViewRepository stockMovementViewRepository;
    private final StockMovementViewMapper stockMovementViewMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public StockMovementViewService(StockMovementViewRepository stockMovementViewRepository,
                                    StockMovementViewMapper stockMovementViewMapper,
                                    OwnershipValidator ownershipValidator,
                                    AuthUtil authUtil) {
        this.stockMovementViewRepository = stockMovementViewRepository;
        this.stockMovementViewMapper = stockMovementViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> findByProperty(
            UUID propertyId, Pageable pageable) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return stockMovementViewRepository.findByPropertyId(propertyId, pageable)
                .map(stockMovementViewMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponse> findByPropertyAndType(
            UUID propertyId, String movementType, Pageable pageable) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return stockMovementViewRepository.findByPropertyIdAndMovementType(
                        propertyId, movementType, pageable)
                .map(stockMovementViewMapper::toResponse);
    }
}