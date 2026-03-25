package tech.agrowerk.infrastructure.repository.farming;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.enums.BatchStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BatchRepository extends JpaRepository<Batch, UUID> {

    Page<Batch> findBySupplier_Id(UUID supplierId, Pageable pageable);

    Page<Batch> findByInput_Id(UUID inputId, Pageable pageable);

    Page<Batch> findByProperty_Id(UUID propertyId, Pageable pageable);

    boolean existsByBatchNumber(String batchNumber);

    boolean existsBySupplier_IdAndProperty_Id(UUID supplierId, UUID propertyId);

    @Query("""
        SELECT b FROM Batch b
        WHERE b.input.id = :inputId
        AND b.status = :status
        AND b.property.id = :propertyId
        AND b.expirationDate > CURRENT_DATE
        ORDER BY b.expirationDate ASC
    """)
    List<Batch> findActiveByInputAndPropertyOrderByExpirationDateAsc(
            @Param("inputId") UUID inputId,
            @Param("propertyId") UUID propertyId,
            @Param("status") BatchStatus status
    );

    @Query("""
        SELECT b FROM Batch b
        WHERE b.property.id = :propertyId
        AND b.status = :status
        AND b.expirationDate <= :alertDate
        AND b.expirationDate > CURRENT_DATE
        ORDER BY b.expirationDate ASC
    """)
    Page<Batch> findNearExpirationByProperty(
            @Param("propertyId") UUID propertyId,
            @Param("status") BatchStatus status,
            @Param("alertDate") LocalDate alertDate,
            Pageable pageable
    );

    @Query("""
        SELECT b FROM Batch b
        WHERE b.property.id = :propertyId
        AND b.status = :status
        AND b.expirationDate < CURRENT_DATE
        AND b.currentQuantity > 0
    """)
    Page<Batch> findExpiredWithRemainingStock(
            @Param("propertyId") UUID propertyId,
            @Param("status") BatchStatus status,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM Batch b
        WHERE b.input.id = :inputId
        AND b.status = :status
        AND b.property.id = :propertyId
        AND b.expirationDate > CURRENT_DATE
        ORDER BY b.expirationDate ASC
    """)
    List<Batch> findActiveForConsumptionWithLock(
            @Param("inputId") UUID inputId,
            @Param("propertyId") UUID propertyId,
            @Param("status") BatchStatus status
    );
}