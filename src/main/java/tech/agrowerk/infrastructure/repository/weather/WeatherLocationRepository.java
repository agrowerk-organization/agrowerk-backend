package tech.agrowerk.infrastructure.repository.weather;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.property.State;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherLocationRepository extends JpaRepository<WeatherLocation, UUID> {

    List<WeatherLocation> findByActiveTrue();

    Optional<WeatherLocation> findByProperty(Property property);

    List<WeatherLocation> findByState(State state);

    Optional<WeatherLocation> findByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);
}
