package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.valueobject.QualityMetrics;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "yields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Yield {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "harvest_id", nullable = false)
    private Harvest harvest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalProducedKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal productivityPerHectare;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "moisturePercentage", column = @Column(name = "moisture_percentage")),
            @AttributeOverride(name = "impurityPercentage", column = @Column(name = "impurity_percentage")),
            @AttributeOverride(name = "qualityGrade", column = @Column(name = "quality_grade"))
    })
    private QualityMetrics qualityMetrics;

    @Column(precision = 10, scale = 2)
    private BigDecimal lossesKg;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}