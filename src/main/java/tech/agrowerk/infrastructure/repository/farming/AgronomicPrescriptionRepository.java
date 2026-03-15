package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.AgronomicPrescription;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgronomicPrescriptionRepository
        extends JpaRepository<AgronomicPrescription, UUID> {

    @Query("""
        SELECT ap FROM AgronomicPrescription ap
        WHERE ap.planting.id = :plantingId
        AND ap.active = true
        AND ap.validUntil >= CURRENT_DATE
        ORDER BY ap.validUntil DESC
    """)
    Optional<AgronomicPrescription> findValidByPlanting(
            @Param("plantingId") UUID plantingId
    );

    List<AgronomicPrescription> findByField_Id(UUID fieldId);

    List<AgronomicPrescription> findByPlanting_Id(UUID plantingId);

    @Query("""
        SELECT ap FROM AgronomicPrescription ap
        WHERE ap.field.property.id = :propertyId
        AND ap.active = true
        AND ap.validUntil BETWEEN CURRENT_DATE AND :alertDate
        ORDER BY ap.validUntil ASC
    """)
    List<AgronomicPrescription> findNearExpirationByProperty(
            @Param("propertyId") UUID propertyId,
            @Param("alertDate") LocalDate alertDate
    );

    boolean existsByPlanting_IdAndActiveTrue(UUID plantingId);
}