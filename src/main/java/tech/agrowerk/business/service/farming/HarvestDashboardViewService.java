package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.views.HarvestDashboardResponse;
import tech.agrowerk.business.mapper.farming.FarmingViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.repository.farming.HarvestDashboardViewRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class HarvestDashboardViewService {

    private final HarvestDashboardViewRepository harvestDashboardViewRepository;
    private final FarmingViewMapper farmingViewMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public HarvestDashboardViewService(HarvestDashboardViewRepository harvestDashboardViewRepository,
                                       FarmingViewMapper farmingViewMapper,
                                       OwnershipValidator ownershipValidator,
                                       AuthUtil authUtil) {
        this.harvestDashboardViewRepository = harvestDashboardViewRepository;
        this.farmingViewMapper = farmingViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional(readOnly = true)
    public List<HarvestDashboardResponse> findByProperty(UUID propertyId) {
        ownershipValidator.validateOwnership(propertyId, authUtil.getAuthenticatedUser().id());

        return harvestDashboardViewRepository.findAllByPropertyId(propertyId)
                .stream()
                .map(farmingViewMapper::toHarvestDashboardResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<HarvestDashboardResponse> findByPlanting(UUID plantingId) {
        return harvestDashboardViewRepository.findByPlantingId(plantingId)
                .map(view -> {
                    ownershipValidator.validateOwnership(
                            view.getPropertyId(), authUtil.getAuthenticatedUser().id());
                    return farmingViewMapper.toHarvestDashboardResponse(view);
                });
    }
}