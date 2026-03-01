package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.views.ActivePlantingView;

import java.util.UUID;

@Repository
public interface ActivePlantingViewRepository extends JpaRepository<ActivePlantingView, UUID> {

    ActivePlantingView findByPlantingId(UUID plantingId);
}
