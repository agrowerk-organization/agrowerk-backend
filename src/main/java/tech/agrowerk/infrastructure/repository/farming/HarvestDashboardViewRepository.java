package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.views.HarvestDashboardView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HarvestDashboardViewRepository extends JpaRepository<HarvestDashboardView, UUID> {
    Optional<HarvestDashboardView> findByPlantingId(UUID plantingId);
    List<HarvestDashboardView> findAllByPropertyId(UUID propertyId);
}
