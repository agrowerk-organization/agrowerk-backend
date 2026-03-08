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
        SELECT COALESCE(SUM(h.quantityKg), 0) FROM Harvest h
        WHERE h.planting.property.id = :propertyId
        AND h.harvestDate BETWEEN :start AND :end
    """)
    BigDecimal sumQuantityByPropertyAndPeriod(
            @Param("propertyId") UUID propertyId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}
