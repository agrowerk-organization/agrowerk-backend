package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.Warehouse;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    List<Warehouse> findByProperty_IdAndIsActiveTrue(UUID propertyId);

    boolean existsByNameIgnoreCaseAndProperty_Id(String name, UUID propertyId);

    boolean existsByCodeAndProperty_Id(String code, UUID propertyId);

    @Query("""
        SELECT COUNT(s) > 0 FROM Stock s
        WHERE s.warehouse.id = :warehouseId
        AND s.currentQuantity > 0
    """)
    boolean hasActiveStock(@Param("warehouseId") UUID warehouseId);
}
