package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.farming.enums.FieldStatus;
import tech.agrowerk.infrastructure.model.farming.enums.SoilType;
import tech.agrowerk.infrastructure.model.property.Property;
import tech.agrowerk.infrastructure.model.valueobject.Geolocation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fields", indexes = {
        @Index(name = "idx_field_property", columnList = "property_id"),
        @Index(name = "idx_field_status", columnList = "field_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Column(precision = 10, scale = 2)
    private BigDecimal areaHectares;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SoilType soilType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldStatus fieldStatus;

    @Column(precision = 5, scale = 2)
    private BigDecimal slopePercentage;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "geo_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "geo_longitude"))
    })
    private Geolocation geolocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (fieldStatus == null) {
            fieldStatus = FieldStatus.ACTIVE;
        }
    }
}
