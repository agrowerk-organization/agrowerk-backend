package tech.agrowerk.infrastructure.model.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.farming.Harvest;
import tech.agrowerk.infrastructure.model.property.Property;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"property_id", "input_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne
    @JoinColumn(name = "input_id", nullable = false)
    private Input input;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal currentQuantity = BigDecimal.ZERO;

    @Column(precision = 10, scale = 3)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(precision = 10, scale = 3)
    private BigDecimal availableQuantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal weightedAverageCost;

    @Column(name = "last_entry_date")
    private LocalDateTime lastEntryDate;

    @Column(name = "last_exit_date")
    private LocalDateTime lastExitDate;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "stock")
    private List<Harvest> harvests;
}