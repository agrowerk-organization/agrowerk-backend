package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Planting;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlantingRepository extends JpaRepository<Planting, UUID> {

    Page<Planting> findByProperty_Id(UUID propertyId, Pageable pageable);

    List<Planting> findBySeason_IdAndPlantingStatus(UUID seasonId, PlantingStatus plantingStatus);

    List<Planting> findByField_IdAndPlantingStatus(UUID fieldId, PlantingStatus plantingStatus);

    boolean existsByField_IdAndPlantingStatus(UUID fieldId, PlantingStatus plantingStatus);

    @Query("""
        SELECT COALESCE(SUM(p.areaHectares), 0) FROM Planting p
        WHERE p.field.id = :fieldId
        AND p.plantingStatus =  'IN_PROGRESS'
    """)
    BigDecimal sumActivePlantingAreaByField(@Param("fieldId") UUID fieldId);

}
