package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.enums.ToxicologicalClass;
import tech.agrowerk.infrastructure.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inputs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Input {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(unique = true, length = 50)
    private String internalCode;

    @Column(length = 50)
    private String manufacturerCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private InputCategory category;

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


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "input")
    private List<Stock> stocks;

    @OneToMany(mappedBy = "input")
    private List<Batch> batches;

    @OneToMany(mappedBy = "input")
    private List<PlantingInput> plantingInputs;
}
