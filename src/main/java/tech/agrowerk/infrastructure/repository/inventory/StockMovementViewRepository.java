package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;

import java.util.UUID;

@Repository
public interface StockMovementViewRepository extends JpaRepository<StockMovement, UUID> {
}
