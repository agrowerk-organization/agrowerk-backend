package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Planting;

import java.util.UUID;

@Repository
public interface PlantingRepository extends JpaRepository<Planting, UUID> {
}
