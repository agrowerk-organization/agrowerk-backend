package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.PlantingInput;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlantingInputRepository extends JpaRepository<PlantingInput, UUID> {

    Page<PlantingInput> findByPlanting_Id(UUID plantingId, Pageable pageable);

    Page<PlantingInput> findByInput_Id(UUID inputId, Pageable pageable);

    boolean existsByPlanting_Id(UUID plantingId);

    @Query("""
        SELECT pi FROM PlantingInput pi
        WHERE pi.input.id = :inputId
        AND pi.planting.property.id IN (
            SELECT up.property.id FROM UserProperty up
            WHERE up.user.id = :userId
            AND up.isActive = true
        )
    """)
    Page<PlantingInput> findByInputIdAndUserId(
            @Param("inputId") UUID inputId,
            @Param("userId") UUID userId,
            Pageable pageable
    );
    @Query("""
        SELECT COALESCE(SUM(pi.quantity), 0) FROM PlantingInput pi
        WHERE pi.planting.id = :plantingId
        AND pi.input.id = :inputId
    """)
    BigDecimal sumQuantityByPlantingAndInput(
            @Param("plantingId") UUID plantingId,
            @Param("inputId") UUID inputId
    );

}
