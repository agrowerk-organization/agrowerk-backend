package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.PrescriptionItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, UUID> {
    @Query("""
        SELECT pi FROM PrescriptionItem pi
        WHERE pi.input.id = :inputId
        AND pi.prescription.planting.id = : plantingId
        AND pi.prescription.active = true
        AND pi.prescription.validUntil >= CURRENT_DATE
    """)
    Optional<PrescriptionItem> findValidByInputAndPlanting(
            @Param("inputId") UUID inputId,
            @Param("plantingId") UUID plantingId
    );

    List<PrescriptionItem> findByPrescription_Id(UUID prescriptionId);

    boolean existsByPrescription_IdAndInput_Id(UUID prescriptionId, UUID inputId);
}
