package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.views.StockPositionView;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockPositionViewRepository extends JpaRepository<StockPositionView, UUID> {
     List<StockPositionView> findByPropertyIdAndStockAlert(UUID propertyId, String alert);

     List<StockPositionView> findByPropertyId(UUID propertyId);
}
