package tech.agrowerk.infrastructure.model.barter.views;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Subselect;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Immutable
@Subselect("""
    SELECT
        user_id,
        CAST(user_id AS VARCHAR) AS id,
        COUNT(*) AS total_transactions,
        COUNT(*) FILTER (WHERE status = 'COMPLETED')   AS completed,
        COUNT(*) FILTER (WHERE status = 'IN_PROGRESS') AS in_progress,
        COUNT(*) FILTER (WHERE status = 'CANCELLED')   AS cancelled,
        COUNT(*) FILTER (WHERE status = 'DISPUTED')    AS disputed,
        SUM(total_value)                  AS total_value_traded
    FROM (
        SELECT offeror_id  AS user_id, status,
               (offeror_crop_quantity * 1) AS total_value
        FROM barter_transactions
        UNION ALL
        SELECT acceptor_id AS user_id, status,
               (acceptor_crop_quantity * 1) AS total_value
        FROM barter_transactions
    ) t
    GROUP BY user_id
""")
@Getter
public class UserTransactionSummaryView {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "total_transactions")
    private Long totalTransactions;

    @Column(name = "completed")
    private Long completed;

    @Column(name = "in_progress")
    private Long inProgress;

    @Column(name = "cancelled")
    private Long cancelled;

    @Column(name = "disputed")
    private Long disputed;

    @Column(name = "total_value_traded")
    private BigDecimal totalValueTraded;
}