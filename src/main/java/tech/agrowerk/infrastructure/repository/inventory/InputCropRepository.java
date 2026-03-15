package tech.agrowerk.infrastructure.repository.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.inventory.InputCrop;

import java.util.List;
import java.util.UUID;

@Repository
public interface InputCropRepository extends JpaRepository<InputCrop, UUID> {
    List<InputCrop> findByCrop_IdAndApprovedByAdminTrue(UUID cropId);

    List<InputCrop> findByInput_Id(UUID inputId);

    List<InputCrop> findByApprovedByAdminFalse();

    boolean existsByInput_IdAndCrop_Id(UUID inputId, UUID cropId);

    @Query("""
        SELECT ic FROM InputCrop ic
        WHERE ic.input.supplier.id = :supplierId
        AND ic.approvedByAdmin = false
    """)
    List<InputCrop> findPendingBySupplierId(
            @Param("supplierId") UUID supplierId
    );
}
