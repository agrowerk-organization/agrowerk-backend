package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Harvest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HarvestRepository extends JpaRepository<Harvest, UUID> {

    Optional<Harvest> findByPlanting_Id(UUID plantingId);

    Page<Harvest> findByPlanting_Property_Id(UUID propertyId, Pageable pageable);

    boolean existsByPlanting_Id(UUID plantingId);

    Page<Harvest> findByPlanting_Property_IdAndHarvestDate(UUID propertyId, LocalDate harvestDate, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(hp.quantityKg), 0)
        FROM Harvest h
        JOIN h.partials hp
        WHERE h.planting.id = :plantingId
    """)
    BigDecimal sumTotalQuantityByPlantingId(@Param("plantingId") UUID plantingId);

    @Query("SELECT h FROM Harvest h LEFT JOIN FETCH h.partials WHERE h.planting.id = :plantingId")
    Optional<Harvest> findByPlanting_IdWithPartials(@Param("plantingId") UUID plantingId);

    @Query("""
        SELECT COALESCE(SUM(hp.quantityKg), 0)
        FROM Harvest h
        JOIN h.partials hp
        WHERE h.planting.property.id = :propertyId
        AND h.harvestDate BETWEEN :start AND :end
    """)
    BigDecimal sumQuantityByPropertyAndPeriod(
            @Param("propertyId") UUID propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
