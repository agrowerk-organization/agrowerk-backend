package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.views.StockPositionView;

import java.util.Arrays;
import java.util.UUID;

@Repository
public interface StockPositionViewRepository extends JpaRepository<StockPositionView, UUID> {
   // Page<StockMovement> findByPropertyId(UUID propertyId);
}
