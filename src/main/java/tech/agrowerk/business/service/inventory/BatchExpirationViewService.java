package tech.agrowerk.business.service.inventory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.views.BatchExpirationResponse;
import tech.agrowerk.business.mapper.inventory.BatchExpirationViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.inventory.BatchExpirationViewRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BatchExpirationViewService {

    private final BatchExpirationViewRepository batchExpirationViewRepository;
    private final BatchExpirationViewMapper batchExpirationViewMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public BatchExpirationViewService(BatchExpirationViewRepository batchExpirationViewRepository,
                                      BatchExpirationViewMapper batchExpirationViewMapper,
                                      OwnershipValidator ownershipValidator,
                                      AuthUtil authUtil) {
        this.batchExpirationViewRepository = batchExpirationViewRepository;
        this.batchExpirationViewMapper = batchExpirationViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    public List<BatchExpirationResponse> findByProperty(UUID propertyId) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return batchExpirationViewRepository.findByPropertyId(propertyId)
                .stream()
                .map(batchExpirationViewMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BatchExpirationResponse> findCritical(UUID propertyId) {
        ownershipValidator.validateOwnership(
                propertyId, authUtil.getAuthenticatedUser().id());

        return batchExpirationViewRepository.findByPropertyIdAndExpirationStatus(
                        propertyId, "CRITICAL")
                .stream()
                .map(batchExpirationViewMapper::toResponse)
                .toList();
    }
}