package tech.agrowerk.infrastructure.model.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.farming.enums.ToxicologicalClass;
import tech.agrowerk.infrastructure.model.shared_enums.UnitOfMeasure;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.PlantingInput;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inputs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Input {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(unique = true, length = 50)
    private String internalCode;

    @Column(length = 50)
    private String manufacturerCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitOfMeasure unitOfMeasure;

    @Column(length = 100)
    private String activeIngredient;

    @Column(length = 100)
    private String formulation;

    @Column(length = 50)
    private String concentration;

    @Column(length = 50)
    private String mapaRegistration;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ToxicologicalClass toxicologicalClass;

    @Column
    private Integer gracePeriod;

    @Column(precision = 10, scale = 2)
    private BigDecimal minimumStock;

    @Column(precision = 10, scale = 2)
    private BigDecimal maximumStock;

    @Column(precision = 10, scale = 2)
    private BigDecimal averagePurchasePrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal lastPurchasePrice;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Boolean controlled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private InputCategory category;

    @OneToMany(mappedBy = "input", fetch = FetchType.LAZY)
    private List<Stock> stocks;

    @OneToMany(mappedBy = "input", fetch = FetchType.LAZY)
    private List<Batch> batches;

    @OneToMany(mappedBy = "input", fetch = FetchType.LAZY)
    private List<PlantingInput> plantingInputs;

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
