package tech.agrowerk.infrastructure.repository.weather;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<WeatherLocation> findAllByActiveTrue();

    Optional<WeatherLocation> findByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);

    @Query("SELECT wl FROM WeatherLocation wl " +
            "JOIN FETCH wl.property p " +
            "JOIN UserProperty up ON up.property.id = p.id " +
            "WHERE up.user.id = :userId")
    Page<WeatherLocation> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);
}
