package tech.agrowerk.infrastructure.repository.weather;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.agrowerk.infrastructure.model.weather.WeatherForecast;
import tech.agrowerk.infrastructure.model.weather.WeatherLocation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, UUID> {

    List<WeatherForecast> findByLocationAndForecastDateBetweenAndForecastHourIsNull(
            WeatherLocation location,
            LocalDate startDate,
            LocalDate endDate
    );

    List<WeatherForecast> findByLocationAndForecastDateAndForecastHourIsNotNull(
            WeatherLocation location,
            LocalDate date
    );

    void deleteByLocationAndForecastDateBetween(WeatherLocation location, LocalDate start, LocalDate end);

    void deleteByForecastDateBefore(LocalDate date);

    Page<WeatherForecast> findByLocationAndForecastHourIsNullOrderByForecastDateDesc(
            WeatherLocation location, Pageable pageable
    );

    @Query("""
            SELECT wf FROM WeatherForecast wf
            JOIN FETCH wf.location wl
            JOIN UserProperty up ON up.property.id = wl.property.id
            WHERE up.user.id = :userId
            AND wf.forecastHour IS NULL
            AND wf.forecastDate BETWEEN :start AND :end
            ORDER BY wf.forecastDate ASC
       """)
    Page<WeatherForecast> findDailyForecastsByUserId(
            @Param("userId") UUID userId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable
    );
}