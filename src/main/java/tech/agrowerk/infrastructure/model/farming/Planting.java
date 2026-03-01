package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.farming.enums.PlantingStatus;
import tech.agrowerk.infrastructure.model.property.Property;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "plantings", indexes = {
        @Index(name = "idx_planting_property", columnList = "property_id"),
        @Index(name = "idx_planting_season", columnList = "season_id"),
        @Index(name = "idx_planting_field", columnList = "field_id"),
        @Index(name = "idx_planting_status", columnList = "planting_status"),
        @Index(name = "idx_planting_crop", columnList = "crop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Planting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal areaHectares;

    @Column(nullable = false)
    private LocalDate plantingDate;

    @Column(nullable = false)
    private LocalDate expectedHarvestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlantingStatus plantingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToMany(mappedBy = "planting")
    private List<PlantingInput> plantingInputs;

    @OneToOne(mappedBy = "planting")
    private Harvest harvest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_variety_id", nullable = false)
    private CropVariety cropVariety;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @OneToMany(mappedBy = "planting", fetch = FetchType.LAZY)
    private List<AgriculturalPractice> agriculturalPractices;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

}
