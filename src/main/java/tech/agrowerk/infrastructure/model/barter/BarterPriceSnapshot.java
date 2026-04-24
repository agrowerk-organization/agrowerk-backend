package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "barter_price_snapshots", indexes = {
        @Index(name = "idx_bps_transaction_id",     columnList = "transaction_id", unique = true),
        @Index(name = "idx_bps_ptax_reference_date", columnList = "ptax_reference_date"),
        @Index(name = "idx_bps_commodity",           columnList = "commodity")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BarterPriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String commodity;

    @Column(nullable = false, length = 10)
    private String cbotContractMonth;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal cbotPriceUsd;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal ptaxRate;

    @Column(nullable = false)
    private LocalDate ptaxReferenceDate;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal basisUsd;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bagPriceBrl;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalValueBrl;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal totalBagsDue;

    @Column(nullable = false)
    private LocalDateTime snapshotAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private BarterTransaction transaction;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
