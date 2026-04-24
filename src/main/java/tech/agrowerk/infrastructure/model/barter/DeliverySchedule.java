package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.barter.enums.DeliveryStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_schedules", indexes = {
        @Index(name = "idx_ds_transaction_id",  columnList = "transaction_id"),
        @Index(name = "idx_ds_commitment_id",   columnList = "commitment_id"),
        @Index(name = "idx_ds_scheduled_date",  columnList = "scheduled_date"),
        @Index(name = "idx_ds_status",          columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private BarterTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commitment_id")
    private CropCommitment commitment;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal scheduledQuantity;

    @Column(precision = 10, scale = 2)
    private BigDecimal deliveredQuantity;

    private LocalDate actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = DeliveryStatus.SCHEDULED;
        }
    }
}