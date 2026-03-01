package tech.agrowerk.infrastructure.repository.farming;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.Season;
import tech.agrowerk.infrastructure.model.farming.enums.SeasonStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeasonRepository extends JpaRepository<Season, UUID> {

    List<Season> findByPropertyId(UUID propertyUd);

    Optional<Season> findByPropertyIdAndSeasonStatus(UUID propertyId, SeasonStatus seasonStatus);

    @Query("""
        SELECT COUNT(s) > 0 FROM Season s
        WHERE s.property.id = :propertyId
        AND s.seasonStatus != 'FINISHED'
        AND s.startDate <= :endDate
        AND s.endDate >= :startDate
    """)
    boolean existsOverlappingSeasons(
            @Param("propertyId") UUID propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
            );

    boolean existsByPropertyIdAndName(UUID propertyId, String propertyName);
}