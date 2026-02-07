package tech.agrowerk.infrastructure.repository.weather;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.weather.WeatherAlert;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;
import tech.agrowerk.infrastructure.model.weather.enums.WeatherAlertSeverity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, UUID> {

    List<WeatherAlert> findByLocationAndIsActiveTrue(WeatherLocation location);

    List<WeatherAlert> findByIsActiveTrueAndNotifiedFalse();

    long countByLocation(WeatherLocation location);

    long countByLocationAndSeverity(WeatherLocation location, WeatherAlertSeverity severity);

    @Modifying
    @Transactional
    void deleteByIsActiveFalseAndEndTimeBefore(Instant timestamp);

    List<WeatherAlert> findByIsActiveTrueAndEndTimeBefore(Instant now);
}
