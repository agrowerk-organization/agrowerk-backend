package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterPriceSnapshot;

import java.util.UUID;

@Repository
public interface BarterPriceSnapshotRepository extends JpaRepository<BarterPriceSnapshot, UUID> {
}
