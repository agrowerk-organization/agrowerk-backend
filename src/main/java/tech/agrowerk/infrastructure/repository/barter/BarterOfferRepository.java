package tech.agrowerk.infrastructure.repository.barter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.barter.BarterOffer;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BarterOfferRepository extends JpaRepository<BarterOffer, UUID> {

    Page<BarterOffer> findByStatusOrderByCreatedAtDesc(OfferStatus status, Pageable pageable);

    Page<BarterOffer> findByStatusAndRegionIgnoreCaseOrderByCreatedAtDesc(
            OfferStatus status, String region, Pageable pageable);

    Page<BarterOffer> findByStatusAndOfferedCrop_IdOrderByCreatedAtDesc(
            OfferStatus status, UUID cropId, Pageable pageable);

    Page<BarterOffer> findByStatusAndOfferTypeOrderByCreatedAtDesc(
            OfferStatus status, OfferType offerType, Pageable pageable);

    Page<BarterOffer> findByOwner_IdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);

    List<BarterOffer> findByStatusAndExpiresAtBefore(OfferStatus status, LocalDate date);

    @Modifying
    @Query("UPDATE BarterOffer o SET o.viewCount = o.viewCount + 1 WHERE o.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    boolean existsByOwner_IdAndStatus(UUID ownerId, OfferStatus status);
}