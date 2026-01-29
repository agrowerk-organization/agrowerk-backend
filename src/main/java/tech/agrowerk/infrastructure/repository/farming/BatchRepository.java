package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Batch;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
}
