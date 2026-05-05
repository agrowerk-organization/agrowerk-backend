package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.views.StockPositionResponse;
import tech.agrowerk.business.mapper.inventory.StockPositionViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.inventory.StockPositionViewRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class StockPositionViewService {

    private final StockPositionViewRepository stockPositionViewRepository;
    private final StockPositionViewMapper stockPositionViewMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public StockPositionViewService(StockPositionViewRepository stockPositionViewRepository,
                                    StockPositionViewMapper stockPositionViewMapper,
                                    OwnershipValidator ownershipValidator,
                                    AuthUtil authUtil) {
        this.stockPositionViewRepository = stockPositionViewRepository;
        this.stockPositionViewMapper = stockPositionViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional(readOnly = true)
    public List<StockPositionResponse> findByProperty(UUID propertyId) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return stockPositionViewRepository.findByPropertyId(propertyId)
                .stream()
                .map(stockPositionViewMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockPositionResponse> findByPropertyAndAlert(
            UUID propertyId, String alert) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return stockPositionViewRepository.findByPropertyIdAndStockAlert(propertyId, alert)
                .stream()
                .map(stockPositionViewMapper::toResponse)
                .toList();
    }
}