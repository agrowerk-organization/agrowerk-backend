package tech.agrowerk.infrastructure.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
}