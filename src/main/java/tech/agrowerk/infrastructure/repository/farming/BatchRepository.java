package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("""
        SELECT b FROM Batch b
        WHERE b.input.id = :inputId
        AND b.status = :status
        AND b.expirationDate > CURRENT_DATE
        ORDER BY b.expirationDate ASC
    """)
    List<Batch> findActiveByInputOrderByExpirationDateAsc(
            @Param("inputId") UUID inputId,
            @Param("status") BatchStatus status
    );

    @Query("""
        SELECT b FROM Batch b
        JOIN b.movements m
        WHERE m.property.id = :propertyId
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
        WHERE b.status = :status
        AND b.expirationDate < CURRENT_DATE
        AND b.currentQuantity > 0
    """)
    List<Batch> findExpiredWithRemainingStock(
            @Param("status") BatchStatus status
    );

    @Query("""
        SELECT b FROM Batch b
        WHERE b.supplier.id = :supplierId
        AND b.status = :status
        ORDER BY b.expirationDate ASC
    """)
    Page<Batch> findBySupplierId(
            @Param("supplierId") UUID supplierId,
            @Param("status") BatchStatus status,
            Pageable pageable
    );
}