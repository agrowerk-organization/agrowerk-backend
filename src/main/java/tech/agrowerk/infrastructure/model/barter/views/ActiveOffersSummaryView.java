package tech.agrowerk.infrastructure.model.barter.views;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Immutable
@Subselect("""
    SELECT
        CAST(CONCAT(offered_crop_id::text, '-', COALESCE(region, 'NACIONAL')) AS VARCHAR) AS id,
        offered_crop_id,
        region,
        COUNT(*)                          AS total_offers,
        SUM(offered_crop_quantity)        AS total_quantity,
        AVG(requested_value)              AS avg_requested_value,
        MIN(expires_at)                   AS nearest_expiration
    FROM barter_offers
    WHERE status = 'ACTIVE'
    GROUP BY offered_crop_id, region
""")
@Synchronize({"barter_offers"})
@Getter
public class ActiveOffersSummaryView {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "offered_crop_id")
    private UUID offeredCropId;

    @Column(name = "region")
    private String region;

    @Column(name = "total_offers")
    private Long totalOffers;

    @Column(name = "total_quantity")
    private BigDecimal totalQuantity;

    @Column(name = "avg_requested_value")
    private BigDecimal avgRequestedValue;

    @Column(name = "nearest_expiration")
    private java.time.LocalDate nearestExpiration;
}