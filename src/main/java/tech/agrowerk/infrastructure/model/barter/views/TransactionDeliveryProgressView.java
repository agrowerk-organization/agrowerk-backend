package tech.agrowerk.infrastructure.model.barter.views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Immutable
@Subselect("""
    SELECT
        cc.transaction_id                                             AS id,
        cc.transaction_id,
        COUNT(cc.id)                                                  AS total_commitments,
        COUNT(cc.id) FILTER (WHERE cc.status = 'DELIVERED')          AS delivered_commitments,
        COUNT(cc.id) FILTER (WHERE cc.status = 'PARTIALLY_DELIVERED') AS partial_commitments,
        COUNT(cc.id) FILTER (WHERE cc.status = 'OVERDUE')            AS overdue_commitments,
        SUM(cc.committed_quantity)                                    AS total_committed,
        SUM(cc.delivered_quantity)                                    AS total_delivered,
        CASE
            WHEN SUM(cc.committed_quantity) = 0 THEN 0
            ELSE ROUND(SUM(cc.delivered_quantity) * 100.0
                 / SUM(cc.committed_quantity), 2)
        END AS delivery_progress_pct
    FROM crop_commitments cc
    GROUP BY cc.transaction_id
""")
@Getter
public class TransactionDeliveryProgressView {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "total_commitments")
    private Long totalCommitments;

    @Column(name = "delivered_commitments")
    private Long deliveredCommitments;

    @Column(name = "partial_commitments")
    private Long partialCommitments;

    @Column(name = "overdue_commitments")
    private Long overdueCommitments;

    @Column(name = "total_committed")
    private BigDecimal totalCommitted;

    @Column(name = "total_delivered")
    private BigDecimal totalDelivered;

    @Column(name = "delivery_progress_pct")
    private BigDecimal deliveryProgressPct;
}
