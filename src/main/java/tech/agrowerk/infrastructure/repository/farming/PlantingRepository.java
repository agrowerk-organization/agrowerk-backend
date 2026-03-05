package tech.agrowerk.infrastructure.repository.farming;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlantingRepository extends JpaRepository<Planting, UUID> {

    Page<Planting> findByPropertyId(UUID propertyId, Pageable pageable);

    List<Planting> findBySeasonIdAndPlantingStatus(UUID seasonId, PlantingStatus plantingStatus);

    List<Planting> findByFieldIdAndPlantingStatus(UUID fieldId, PlantingStatus plantingStatus);

    boolean existsByFieldIdAndPlantingStatus(UUID fieldId, PlantingStatus plantingStatus);

    @Query("""
        SELECT COALESCE(SUM(p.areaHectares), 0) FROM Planting p
        WHERE p.field.id = :fieldId
        AND p.plantingStatus =  'IN_PROGRESS'
    """)
    BigDecimal sumActivePlantingAreaByField(@Param("fieldId") UUID fieldId);

}
