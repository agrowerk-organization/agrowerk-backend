package tech.agrowerk.infrastructure.model.barter;


import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.inventory.enums.AssetCategory;
import tech.agrowerk.infrastructure.model.farming.Crop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "barter_exchange_rates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarterExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCategory assetCategory;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cropPricePerKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal assetReferenceValue;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    private LocalDate validFrom;

    @Column(nullable = false)
    private LocalDate validUntil;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    private String region;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
