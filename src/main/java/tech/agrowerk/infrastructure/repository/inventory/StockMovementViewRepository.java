package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;
import tech.agrowerk.infrastructure.model.inventory.views.StockMovementView;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockMovementViewRepository extends JpaRepository<StockMovementView, UUID> {

    Page<StockMovementView> findByPropertyId(UUID propertyId, Pageable pageable);

    Page<StockMovementView> findByPropertyIdAndMovementType(UUID propertyId, String movementType, Pageable pageable);

}
