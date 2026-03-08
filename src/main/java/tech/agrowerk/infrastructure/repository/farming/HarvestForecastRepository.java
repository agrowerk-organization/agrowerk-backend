package tech.agrowerk.infrastructure.repository.farming;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.farming.HarvestForecast;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HarvestForecastRepository extends JpaRepository<HarvestForecast, UUID> {

    Page<HarvestForecast> findByPlanting_Id(UUID plantingId, Pageable pageable);

    Optional<HarvestForecast> findByPlanting_IdAndForecastDate(UUID plantingId, LocalDate forecastDate);

    Page<HarvestForecast> findByPlanting_Property_IdAndPlanting_Season_Id(UUID propertyId, UUID seasonId, Pageable pageable);

    @Query("""
        SELECT hf FROM HarvestForecast hf
            WHERE hf.planting.season.id = :seasonId
            AND hf.planting.property.id = :propertyId
            AND hf.planting.cropVariety.crop.id = :cropId
            ORDER BY hf.forecastDate DESC
    """)
    Page<HarvestForecast> findLatestByCropAndSeason(
            @Param("seasonId") UUID seasonId,
            @Param("propertyId") UUID propertyId,
            @Param("cropId") UUID cropId,
            Pageable pageable
    );
}
