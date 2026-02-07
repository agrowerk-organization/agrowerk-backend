package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import lombok.*;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;
import tech.agrowerk.infrastructure.model.barter.enums.TransactionStatus;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "barter_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class BarterTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private BarterOffer offer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offeror_id", nullable = false)
    private User offeror;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptor_id", nullable = false)
    private User acceptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType offerorGives;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offeror_crop_id")
    private Crop offerorCrop;

    @Column(precision = 10, scale = 2)
    private BigDecimal offerorCropQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offeror_asset_id")
    private InventoryAsset offerorAsset;

    @Column(precision = 10, scale = 2)
    private BigDecimal offerorAssetQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferType acceptorGives;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptor_crop_id")
    private Crop acceptorCrop;

    @Column(precision = 10, scale = 2)
    private BigDecimal acceptorCropQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptor_asset_id")
    private InventoryAsset acceptorAsset;

    @Column(precision = 10, scale = 2)
    private BigDecimal acceptorAssetQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private LocalDate offerorDeliveryDate;
    private LocalDate acceptorDeliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
    private BarterContract barterContract;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true)
    @Builder.Default
    private List<CropCommitment> cropCommitments = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}