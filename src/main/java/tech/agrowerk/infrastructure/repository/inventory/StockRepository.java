package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Stock findByPropertyIdAndInputId(UUID propertyId,  UUID inputId);

    Optional<Stock> findByProperty_IdAndInput_Id(UUID propertyId, UUID inputId);

    Optional<Stock> findByProperty_IdAndInput_IdAndStockType(UUID propertyId, UUID inputId, StockType stockType);
}
