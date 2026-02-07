package tech.agrowerk.infrastructure.repository.weather;

import org.springframework.data.jpa.repository.JpaRepository;
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

    void deleteByForecastDateBefore(LocalDate date);
}