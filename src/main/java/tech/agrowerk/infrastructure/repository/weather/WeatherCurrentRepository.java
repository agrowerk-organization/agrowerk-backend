package tech.agrowerk.infrastructure.repository.weather;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.weather.WeatherCurrent;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherCurrentRepository extends JpaRepository<WeatherCurrent, UUID> {
    Optional<WeatherCurrent> findTopByLocationOrderByTimestampDesc(WeatherLocation location);

    List<WeatherCurrent> findByLocationAndTimestampAfterOrderByTimestampDesc(
            WeatherLocation location,
            LocalDateTime after
    );

    void deleteByTimestampBefore(LocalDateTime timestamp);}
