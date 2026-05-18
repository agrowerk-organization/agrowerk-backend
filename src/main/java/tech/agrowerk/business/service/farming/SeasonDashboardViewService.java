package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.application.dto.views.SeasonDashboardResponse;
import tech.agrowerk.business.mapper.farming.FarmingViewMapper;
import tech.agrowerk.business.utils.AuthUtil;
import tech.agrowerk.business.validators.OwnershipValidator;
import tech.agrowerk.infrastructure.model.farming.views.SeasonDashboardView;
import tech.agrowerk.infrastructure.repository.farming.SeasonDashboardViewRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SeasonDashboardViewService {

    private final SeasonDashboardViewRepository seasonDashboardViewRepository;
    private final FarmingViewMapper farmingViewMapper;
    private final OwnershipValidator ownershipValidator;
    private final AuthUtil authUtil;

    public SeasonDashboardViewService(SeasonDashboardViewRepository seasonDashboardViewRepository,
                                      FarmingViewMapper farmingViewMapper,
                                      OwnershipValidator ownershipValidator,
                                      AuthUtil authUtil) {
        this.seasonDashboardViewRepository = seasonDashboardViewRepository;
        this.farmingViewMapper = farmingViewMapper;
        this.ownershipValidator = ownershipValidator;
        this.authUtil = authUtil;
    }

    @Transactional(readOnly = true)
    public List<SeasonDashboardResponse> getDashboard(UUID propertyId) {
        ownershipValidator.validateOwnership(propertyId, authUtil.getAuthenticatedUser().id());

        List<SeasonDashboardView> results = seasonDashboardViewRepository.findByPropertyId(propertyId);

        if (results.isEmpty()) {
            log.warn("No data found for property id={} — view may need refresh", propertyId);
            return Collections.emptyList();
        }

        return results.stream().map(farmingViewMapper::toSeasonDashboardResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SeasonDashboardResponse> getDashboardBySeason(UUID seasonId) {
        List<SeasonDashboardView> results = seasonDashboardViewRepository.findAllBySeasonId(seasonId);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        UUID propertyId = results.getFirst().getPropertyId();
        ownershipValidator.validateOwnership(propertyId, authUtil.getAuthenticatedUser().id());

        return results.stream().map(farmingViewMapper::toSeasonDashboardResponse).toList();
    }
}