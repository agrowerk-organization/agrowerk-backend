package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.inventory.Stock;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "harvests", indexes = {
        @Index(name = "idx_harvest_date", columnList = "harvest_date"),
        @Index(name = "idx_harvest_planting", columnList = "planting_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Harvest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate harvestDate;

    @Column
    private LocalDate finalizedAt;

    private String qualityGrade;

    @Column(nullable = false)
    private Boolean finalized = false;

    @OneToMany(mappedBy = "harvest", cascade = CascadeType.ALL)
    private List<HarvestPartial> partials;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @OneToOne(mappedBy = "harvest")
    private StockMovement stockMovement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planting_id", nullable = false)
    private Planting planting;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
