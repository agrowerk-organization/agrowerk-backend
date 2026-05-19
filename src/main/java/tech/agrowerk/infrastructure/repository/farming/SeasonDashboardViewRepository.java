package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tech.agrowerk.infrastructure.model.farming.views.SeasonDashboardView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonDashboardViewRepository
        extends JpaRepository<SeasonDashboardView, UUID> {

    @Modifying
    @Transactional
    @Query(value = "REFRESH MATERIALIZED VIEW CONCURRENTLY mv_season_dashboard", nativeQuery = true)
    void refreshSeasonDashboard();

    List<SeasonDashboardView> findByPropertyId(UUID propertyId);

    List<SeasonDashboardView> findAllBySeasonId(UUID seasonId);
}
