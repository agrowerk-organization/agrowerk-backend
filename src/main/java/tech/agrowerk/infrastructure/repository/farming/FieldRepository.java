package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Field;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface FieldRepository extends JpaRepository<Field, UUID> {

    Page<Field> findByProperty_Id(UUID propertyId, Pageable pageable);

    boolean existsByNameIgnoreCaseAndProperty_Id(String name, UUID propertyId);

    List<Field> findByProperty_IdAndFieldStatus(UUID propertyId, FieldStatus fieldStatus);

    @Query("""
            SELECT COALESCE(SUM(f.areaHectares), 0) FROM Field f
            WHERE f.property.id = :propertyId
            AND f.fieldStatus != 'INACTIVE'
    """)
    BigDecimal sumAreaByProperty(@Param("propertyId") UUID propertyId);

    @Query("""
        SELECT COUNT(DISTINCT p.cropVariety.crop.id) FROM Planting p
        WHERE p.field.id = :fieldId
        AND p.plantingStatus = 'ACTIVE'
    """)
    long countActiveCropsInField(@Param("fieldId") UUID fieldId);
}
