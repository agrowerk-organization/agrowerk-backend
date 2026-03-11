package tech.agrowerk.business.service.farming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tech.agrowerk.application.dto.views.SeasonDashboardResponse;
import tech.agrowerk.business.mapper.FarmingViewMapper;
import tech.agrowerk.infrastructure.model.farming.views.SeasonDashboardView;
import tech.agrowerk.infrastructure.repository.farming.SeasonDashboardViewRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SeasonDashboardService {

    private final SeasonDashboardViewRepository seasonDashboardViewRepository;
    private final FarmingViewMapper farmingViewMapper;

    public SeasonDashboardService(SeasonDashboardViewRepository seasonDashboardViewRepository, FarmingViewMapper farmingViewMapper) {
        this.seasonDashboardViewRepository = seasonDashboardViewRepository;
        this.farmingViewMapper = farmingViewMapper;
    }

    @Cacheable(value = "seasonDashboard", key = "#propertyId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    public List<SeasonDashboardResponse> getDashboard(UUID propertyId) {
        List<SeasonDashboardView> results = seasonDashboardViewRepository
                .findByPropertyId(propertyId);

        if (results.isEmpty()) {
            log.warn("No data found for property id={} — view may need refresh", propertyId);
            return Collections.emptyList();
        }

        return results.stream().map(farmingViewMapper::toSeasonDashboardResponse).toList();
    }

    @Cacheable(value = "seasonDashboardDetail", key = "#seasonId",
            cacheManager = "redisCacheManager",
            unless = "#result.isEmpty()")
    public List<SeasonDashboardResponse> getDashboardBySeason(UUID seasonId) {
        log.debug("Fetching dashboard details for season id={}", seasonId);

        List<SeasonDashboardView> results = seasonDashboardViewRepository
                .findBySeasonId(seasonId);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(farmingViewMapper::toSeasonDashboardResponse)
                .toList();
    }
}
