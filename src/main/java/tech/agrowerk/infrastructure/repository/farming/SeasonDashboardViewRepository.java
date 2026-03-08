package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.views.SeasonDashboardView;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeasonDashboardViewRepository
        extends JpaRepository<SeasonDashboardView, UUID> {

    List<SeasonDashboardView> findByProperty_Id(UUID propertyId);
    List<SeasonDashboardView> findBySeason_Id(UUID seasonId);
}
