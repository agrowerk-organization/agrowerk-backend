package tech.agrowerk.infrastructure.model.weather;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "weather_forecasts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WeatherForecast {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private WeatherLocation location;

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "forecast_hour")
    private Integer forecastHour;

    @Column(name = "temperature_min", precision = 5, scale = 2)
    private BigDecimal temperatureMin;

    @Column(name = "temperature_max", precision = 5, scale = 2)
    private BigDecimal temperatureMax;

    @Column(name = "temperature_avg", precision = 5, scale = 2)
    private BigDecimal temperatureAvg;

    @Column(name = "humidity_avg")
    private Integer humidityAvg;

    @Column(name = "humidity_min")
    private Integer humidityMin;

    @Column(name = "humidity_max")
    private Integer humidityMax;

    @Column(name = "rainfall_probability")
    private Integer rainfallProbability;

    @Column(name = "rainfall_amount", precision = 5, scale = 2)
    private BigDecimal rainfallAmount; // mm

    @Column(name = "rainfall_accumulated_7d", precision = 6, scale = 2)
    private BigDecimal rainfallAccumulated7d;

    @Column(name = "wind_speed_avg", precision = 5, scale = 2)
    private BigDecimal windSpeedAvg;

    @Column(name = "wind_speed_max", precision = 5, scale = 2)
    private BigDecimal windSpeedMax;

    @Column(name = "wind_direction")
    private Integer windDirection;

    @Column(name = "uv_index_max", precision = 3, scale = 1)
    private BigDecimal uvIndexMax;

    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    @Column(name = "weather_description", length = 255)
    private String weatherDescription;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(name = "evapotranspiration", precision = 5, scale = 2)
    private BigDecimal evapotranspiration;

    @Column(name = "soil_moisture_0_to_10cm", precision = 5, scale = 2)
    private BigDecimal soilMoisture0to10cm;

    @Column(name = "soil_temperature_0_to_10cm", precision = 5, scale = 2)
    private BigDecimal soilTemperature0to10cm;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
