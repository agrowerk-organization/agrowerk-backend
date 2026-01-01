package tech.agrowerk.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.Batch;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
}
