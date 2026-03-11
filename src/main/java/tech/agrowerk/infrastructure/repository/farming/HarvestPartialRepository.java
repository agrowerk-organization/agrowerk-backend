package tech.agrowerk.infrastructure.repository.farming;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.agrowerk.application.dto.projection.HarvestQuantityProjection;
import tech.agrowerk.infrastructure.model.farming.HarvestPartial;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface HarvestPartialRepository extends JpaRepository<HarvestPartial, UUID> {

    Page<HarvestPartial> findByHarvest_Id(UUID harvestId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(hp.quantityKg), 0) FROM HarvestPartial hp
        WHERE hp.harvest.id = :harvestId
    """)
    BigDecimal sumQuantityByHarvest(@Param("harvestId") UUID harvestId);

    @Query("""
        SELECT new tech.agrowerk.application.dto.projection.HarvestQuantityProjection(
            h.id,
            COALESCE(SUM(hp.quantityKg), 0)
        )
        FROM Harvest h
        LEFT JOIN h.partials hp
        WHERE h.planting.property.id = :propertyId
        GROUP BY h.id
    """)
    List<HarvestQuantityProjection> findAllQuantitiesByProperty(@Param("propertyId") UUID propertyId);

    @Query("""
        SELECT new tech.agrowerk.application.dto.projection.HarvestQuantityProjection(
        hp.harvest.id,
            SUM(hp.quantityKg)
        )
        FROM HarvestPartial hp
        WHERE hp.harvest.planting.property.id = :propertyId
        AND hp.harvest.planting.season.id = :seasonId
        GROUP BY hp.harvest.id
    """)
    Page<HarvestQuantityProjection> findQuantitiesByPropertyAndSeason(
            @Param("propertyId") UUID propertyId,
            @Param("seasonId") UUID seasonId,
            Pageable pageable
    );
}
