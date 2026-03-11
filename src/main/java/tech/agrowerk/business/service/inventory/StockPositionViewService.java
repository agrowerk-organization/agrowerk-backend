package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.inventory.StockPositionViewRepository;

import java.util.UUID;
/*
@Service
@Slf4j
public class StockPositionViewService {

    private final StockPositionViewRepository repository;
  //  private final StockPositionViewMapper mapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

   / public StockPositionViewService(StockPositionViewRepository repository, StockPositionViewMapper mapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Cacheable(value = "stockPosition", key = "#propertyId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<StockPositionResponse> findByProperty(UUID propertyId) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return repository.findByPropertyId(propertyId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Cacheable(value = "stockPosition",
            key = "#propertyId + ':' + #alert",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<StockPositionResponse> findByPropertyAndAlert(
            UUID propertyId, String alert) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return repository.findByPropertyIdAndStockAlert(propertyId, alert)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
} */