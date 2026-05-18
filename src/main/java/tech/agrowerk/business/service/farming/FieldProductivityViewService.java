package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.views.FieldProductivityResponse;
import tech.agrowerk.business.mapper.farming.FarmingViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.utils.AuthenticatedUser;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.farming.FieldProductivityViewRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FieldProductivityViewService {

    private final FieldProductivityViewRepository fieldProductivityViewRepository;
    private final FarmingViewMapper farmingViewMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public FieldProductivityViewService(FieldProductivityViewRepository fieldProductivityViewRepository, FarmingViewMapper farmingViewMapper, OwnershipValidator ownershipValidator, AuthUtil authUtil) {
        this.fieldProductivityViewRepository = fieldProductivityViewRepository;
        this.farmingViewMapper = farmingViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    public Optional<FieldProductivityResponse> findFieldProductivityViewById(UUID fieldId) {
        return fieldProductivityViewRepository.findByFieldId(fieldId)
                .map(view -> {
                    ownershipValidator.validateOwnership(
                            view.getPropertyId(), authUtil.getAuthenticatedUser().id());
                    return farmingViewMapper.toFieldProductivityResponse(view);
                });
    }
}
