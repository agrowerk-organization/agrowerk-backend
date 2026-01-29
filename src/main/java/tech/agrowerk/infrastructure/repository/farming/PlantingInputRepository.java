package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.PlantingInput;

@Repository
public interface PlantingInputRepository extends JpaRepository<PlantingInput, Long> {
}
