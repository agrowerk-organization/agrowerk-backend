package tech.agrowerk.infrastructure.repository.barter;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterOffer;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BarterOfferRepository extends JpaRepository<BarterOffer, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM BarterOffer  o WHERE o.id = :id")
    Optional<BarterOffer> findByIdWithLock(@Param("id") UUID id);

    @Query("""
        SELECT o FROM BarterOffer o
        JOIN FETCH o.owner
        JOIN FETCH o.property p
        JOIN FETCH p.address a
        JOIN FETCH p.state s
        LEFT JOIN FETCH o.offeredForecast f
        LEFT JOIN FETCH f.crop
        LEFT JOIN FETCH o.offeredAsset
        WHERE o.status = :status
        ORDER BY o.createdAt DESC
    """)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    List<BarterOffer> findActiveWithDetails(@Param("status") OfferStatus status, Pageable pageable);

    @Query("SELECT COUNT(o) FROM BarterOffer o WHERE o.status = :status")
    long countByStatus(@Param("status") OfferStatus status);

    @Query("""
        SELECT o FROM BarterOffer o
        LEFT JOIN FETCH o.owner
        LEFT JOIN FETCH o.property
        LEFT JOIN FETCH o.offeredForecast f
        LEFT JOIN FETCH f.crop
        LEFT JOIN FETCH o.offeredAsset
        WHERE o.status = :status AND o.offerType = :offerType
        ORDER BY o.createdAt DESC
    """)
    List<BarterOffer> findByTypeWithDetails(
            @Param("status") OfferStatus status,
            @Param("offerType") OfferType offerType,
            Pageable pageable);

    @Query("SELECT COUNT(o) FROM BarterOffer o WHERE o.status = :status AND o.offerType = :offerType")
    long countByStatusAndOfferType(@Param("status") OfferStatus status, @Param("offerType") OfferType offerType);

    @Query("""
        SELECT o FROM BarterOffer o
        LEFT JOIN FETCH o.owner
        JOIN FETCH o.property p
        JOIN FETCH p.state s
        LEFT JOIN FETCH o.offeredForecast f
        LEFT JOIN FETCH f.crop
        LEFT JOIN FETCH o.offeredAsset
        WHERE o.owner.id = :ownerId
        ORDER BY o.createdAt DESC
    """)
    List<BarterOffer> findMyOffersWithDetails(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM BarterOffer o WHERE o.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") UUID ownerId);


    @Query("""
        SELECT DISTINCT o FROM BarterOffer o
        LEFT JOIN FETCH o.requestedItems ri
        LEFT JOIN FETCH ri.input
        WHERE o.id IN :ids
    """)
    List<BarterOffer> fetchRequestedItems(@Param("ids") List<UUID> ids);


    @Query("""
        SELECT o FROM BarterOffer o
        LEFT JOIN FETCH o.owner
        JOIN FETCH o.property p
        JOIN FETCH p.state s
        LEFT JOIN FETCH o.offeredForecast f
        LEFT JOIN FETCH f.crop
        LEFT JOIN FETCH o.offeredAsset
        LEFT JOIN FETCH o.requestedItems ri
        LEFT JOIN FETCH ri.input
        WHERE o.id = :id
    """)
    Optional<BarterOffer> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
        SELECT DISTINCT o FROM BarterOffer o
        JOIN FETCH o.owner
        JOIN FETCH o.property p
        JOIN FETCH p.state
        LEFT JOIN FETCH o.offeredForecast f
        LEFT JOIN FETCH f.crop
        JOIN o.requestedItems ri
        WHERE o.status = :status
        AND ri.input.id IN :inputIds
        ORDER BY o.createdAt DESC
    """)
    List<BarterOffer> findActiveWithRequestedInputs(
            @Param("status") OfferStatus status,
            @Param("inputIds") List<UUID> inputIds,
            Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT o.id) FROM BarterOffer o
        JOIN o.requestedItems ri
        WHERE o.status = 'ACTIVE'
        AND ri.input.id IN :inputIds
    """)
    long countActiveWithRequestedInputs(@Param("inputIds") List<UUID> inputIds);


    Page<BarterOffer> findByStatusAndOfferedForecast_IdOrderByCreatedAtDesc(
            OfferStatus status, UUID forecastId, Pageable pageable);

    List<BarterOffer> findByStatusAndExpiresAtBefore(OfferStatus status, LocalDate date);

    @Modifying
    @Query("UPDATE BarterOffer o SET o.viewCount = o.viewCount + 1 WHERE o.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    boolean existsByOwner_IdAndStatus(UUID ownerId, OfferStatus status);
}