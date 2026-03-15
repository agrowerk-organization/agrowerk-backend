package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.enums.StockType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockRepository extends JpaRepository<Stock, UUID> {
    Stock findByProperty_IdAndInput_Id(UUID propertyId,  UUID inputId);

    Optional<Stock> findByProperty_IdAndInput_IdAndStockType(UUID propertyId, UUID inputId, StockType stockType);

    Optional<Stock> findByProperty_IdAndStockType(UUID propertyId, StockType stockType);

    List<Stock> findAllByProperty_IdAndStockType(UUID propertyId, StockType stockType);

    List<Stock> findByProperty_Id(UUID propertyId);

    @Query("""
        SELECT s FROM Stock s
        WHERE s.property.id = :propertyId
        AND s.input IS NOT NULL
        AND s.input.minimumStock IS NOT NULL
        AND s.currentQuantity <= s.input.minimumStock
    """)
    List<Stock> findBelowMinimumByProperty(
            @Param("propertyId") UUID propertyId
    );

    @Query("""
        SELECT s FROM Stock s
        WHERE s.property.id = :propertyId
        AND s.input IS NOT NULL
        AND s.input.maximumStock IS NOT NULL
        AND s.currentQuantity >= s.input.maximumStock
    """)
    List<Stock> findAboveMaximumByProperty(
            @Param("propertyId") UUID propertyId
    );
}
