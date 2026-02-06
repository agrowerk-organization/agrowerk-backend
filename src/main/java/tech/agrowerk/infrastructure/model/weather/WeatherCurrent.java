package tech.agrowerk.infrastructure.model.weather;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "weather_currents")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class WeatherCurrent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private WeatherLocation location;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal temperature;

    @Column(name = "feels_like", precision = 5, scale = 2)
    private BigDecimal feelsLike;

    @Column
    private Integer humidity;

    @Column
    private Integer pressure;

    @Column(name = "wind_speed", precision = 5, scale = 2)
    private BigDecimal windSpeed;

    @Column(name = "wind_direction")
    private Integer windDirection;

    @Column(name = "wind_gusts", precision = 5, scale = 2)
    private BigDecimal windGusts;

    @Column
    private Integer clouds;

    @Column
    private Integer visibility;

    @Column(name = "uv_index", precision = 3, scale = 1)
    private BigDecimal uvIndex;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rainfall = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal snowfall = BigDecimal.ZERO;

    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    @Column(name = "weather_description", length = 255)
    private String weatherDescription;

    @Column(name = "weather_code")
    private Integer weatherCode;

    @Column(length = 50)
    @Builder.Default
    private String source = "open-meteo";

    @Column(name = "data_quality_score", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal dataQualityScore = BigDecimal.ONE;

    @Column(name = "fetch_latency_ms")
    private Integer fetchLatencyMs;

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
