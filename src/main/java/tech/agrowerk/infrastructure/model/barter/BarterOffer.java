package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import tech.agrowerk.infrastructure.model.barter.enums.OfferStatus;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.HarvestForecast;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;
import tech.agrowerk.infrastructure.model.property.Property;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "barter_offers", indexes = {
        @Index(name = "idx_bo_status",              columnList = "status"),
        @Index(name = "idx_bo_status_created",      columnList = "status, created_at DESC"),
        @Index(name = "idx_bo_owner_id",            columnList = "owner_id"),
        @Index(name = "idx_bo_offered_forecast_id", columnList = "offered_forecast_id"),
        @Index(name = "idx_bo_expires_at",          columnList = "expires_at"),
        @Index(name = "idx_bo_status_offer_type",   columnList = "status, offer_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarterOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType offerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_forecast_id")
    private HarvestForecast offeredForecast;

    @Column(precision = 10, scale = 2)
    private BigDecimal offeredCropQuantity;

    private LocalDate estimatedHarvestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_asset_id")
    private InventoryAsset offeredAsset;

    @Column(precision = 10, scale = 2)
    private BigDecimal offeredAssetQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType requestedType;

    @Column(columnDefinition = "TEXT")
    private String requestedDescription;

    @Column(precision = 10, scale = 2)
    private BigDecimal requestedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    private LocalDate expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer viewCount = 0;

    @OneToMany(mappedBy = "barterOffer", orphanRemoval = false)
    @Builder.Default
    private List<BarterTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "barterOffer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BarterOfferItem> requestedItems = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

}