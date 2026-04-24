package tech.agrowerk.infrastructure.model.barter.views;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;
import org.hibernate.annotations.Synchronize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Immutable
@Subselect("""
    SELECT
        CAST(offered_forecast_id AS VARCHAR) AS id,
        offered_forecast_id,
        COUNT(*)                          AS total_offers,
        SUM(offered_crop_quantity)        AS total_quantity,
        AVG(requested_value)              AS avg_requested_value,
        MIN(expires_at)                   AS nearest_expiration
    FROM barter_offers
    WHERE status = 'ACTIVE'
    GROUP BY offered_forecast_id
""")
@Synchronize({"barter_offers"})
@Getter
public class ActiveOffersSummaryView {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "offered_forecast_id")
    private UUID offeredForecastId;

    @Column(name = "total_offers")
    private Long totalOffers;

    @Column(name = "total_quantity")
    private BigDecimal totalQuantity;

    @Column(name = "avg_requested_value")
    private BigDecimal avgRequestedValue;

    @Column(name = "nearest_expiration")
    private LocalDate nearestExpiration;
}