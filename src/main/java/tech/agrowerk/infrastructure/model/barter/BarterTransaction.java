package tech.agrowerk.infrastructure.model.barter;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.*;
import tech.agrowerk.infrastructure.model.barter.enums.OfferType;
import tech.agrowerk.infrastructure.model.barter.enums.TransactionStatus;
import tech.agrowerk.infrastructure.model.core.User;
import tech.agrowerk.infrastructure.model.farming.Batch;
import tech.agrowerk.infrastructure.model.farming.Crop;
import tech.agrowerk.infrastructure.model.inventory.InventoryAsset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "barter_transactions", indexes = {
        @Index(name = "idx_bt_offer_id",            columnList = "barter_offer_id"),
        @Index(name = "idx_bt_offeror_id",          columnList = "offeror_id"),
        @Index(name = "idx_bt_acceptor_id",         columnList = "acceptor_id"),
        @Index(name = "idx_bt_status",              columnList = "status"),
        @Index(name = "idx_bt_offer_offeror_status",columnList = "barter_offer_id, offeror_id, status"),
        @Index(name = "idx_bt_offer_status",        columnList = "barter_offer_id, status")
})
@NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@Builder
public class BarterTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offeror_batch_id")
    private Batch offerorBatch;

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

    @OneToOne(mappedBy = "transaction")
    private BarterContract barterContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barter_offer_id", nullable = false)
    private BarterOffer barterOffer;

    @OneToMany(mappedBy = "transaction", orphanRemoval = true)
    @Builder.Default
    private List<CropCommitment> cropCommitments = new ArrayList<>();

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private BarterPriceSnapshot priceSnapshot;

    @OneToMany(mappedBy = "barterTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BarterTransactionItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

}