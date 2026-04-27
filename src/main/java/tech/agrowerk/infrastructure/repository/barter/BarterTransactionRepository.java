package tech.agrowerk.infrastructure.repository.barter;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterTransaction;
import tech.agrowerk.infrastructure.model.barter.enums.TransactionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarterTransactionRepository extends JpaRepository<BarterTransaction, UUID> {

    Page<BarterTransaction> findByOfferor_IdOrderByCreatedAtDesc(UUID offerorId, Pageable pageable);

    Page<BarterTransaction> findByAcceptor_IdOrderByCreatedAtDesc(UUID acceptorId, Pageable pageable);

    boolean existsByBarterOffer_IdAndOfferor_IdAndStatusIn(UUID offerId, UUID offerorId, List<TransactionStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM BarterTransaction t WHERE t.id = :id")
    Optional<BarterTransaction> findByIdWithLock(@Param("id") UUID id);

    @Query(value = """
        SELECT t FROM BarterTransaction t
        LEFT JOIN FETCH t.offeror
        LEFT JOIN FETCH t.acceptor
        LEFT JOIN FETCH t.barterOffer bo
        LEFT JOIN FETCH bo.offeredForecast f
        LEFT JOIN FETCH f.crop
        LEFT JOIN FETCH t.offerorCrop
        LEFT JOIN FETCH t.acceptorCrop
        LEFT JOIN FETCH t.offerorBatch b
        LEFT JOIN FETCH b.input
        LEFT JOIN FETCH t.offerorAsset
        LEFT JOIN FETCH t.barterContract
        LEFT JOIN FETCH b.supplier sup
        LEFT JOIN FETCH sup.address
        WHERE t.offeror.id = :userId OR t.acceptor.id = :userId
        ORDER BY t.createdAt DESC
    """,
            countQuery = """
        SELECT COUNT(t) FROM BarterTransaction t
        WHERE t.offeror.id = :userId OR t.acceptor.id = :userId
    """
    )
    Page<BarterTransaction> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE BarterTransaction t
        SET t.status = 'CANCELLED', t.updatedAt = CURRENT_TIMESTAMP
        WHERE t.barterOffer.id = :offerId
            AND t.id != :acceptedId
            AND t.status = 'PENDING'
    """)
    void cancelAllPendingExcept(@Param("offerId") UUID offerId, @Param("acceptedId") UUID acceptedTransactionId);

}
