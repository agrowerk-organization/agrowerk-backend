package tech.agrowerk.infrastructure.model.farming;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.farming.enums.BatchStatus;
import tech.agrowerk.infrastructure.model.supplier.Supplier;
import tech.agrowerk.infrastructure.model.inventory.Input;
import tech.agrowerk.infrastructure.model.inventory.StockMovement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "batchs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String batchNumber;

    @Column(length = 50)
    private String invoiceNumber;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal initialQuantity;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal currentQuantity;

    @Column(nullable = false)
    private LocalDate manufacturingDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_id", nullable = false)
    private Input input;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @OneToMany(mappedBy = "batch")
    private List<StockMovement> movements;

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

    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    public boolean isNearExpiration(int days) {
        return LocalDate.now().isBefore(expirationDate.plusDays(days));
    }
}
