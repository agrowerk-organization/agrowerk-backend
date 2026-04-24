CREATE OR REPLACE VIEW v_active_offers_summary_view AS
SELECT
    CAST(offered_forecast_id AS VARCHAR) AS id,
    offered_forecast_id,
    COUNT(*) AS total_offers,
    SUM(offered_crop_quantity) AS total_quantity,
    AVG(requested_value) AS avg_requested_value,
    MIN(expires_at) AS nearest_expiration
FROM barter_offers
WHERE status = 'ACTIVE'
GROUP BY offered_forecast_id;


CREATE OR REPLACE VIEW v_overdue_commitments AS
SELECT
    cc.id,
    cc.transaction_id,
    cc.farmer_id,
    cc.crop_id,
    cc.committed_quantity,
    cc.delivered_quantity,
    cc.committed_quantity - cc.delivered_quantity  AS pending_quantity,
    cc.expected_delivery_date,
    CURRENT_DATE - cc.expected_delivery_date       AS days_overdue,
    cc.status
FROM crop_commitments cc
WHERE cc.expected_delivery_date < CURRENT_DATE
  AND cc.status NOT IN ('DELIVERED', 'CANCELLED');

CREATE MATERIALIZED VIEW IF NOT EXISTS mv_user_transaction_summary AS
SELECT
    user_id,
    CAST(user_id AS VARCHAR)          AS id,
    COUNT(*)                          AS total_transactions,
    COUNT(*) FILTER (WHERE status = 'COMPLETED')   AS completed,
    COUNT(*) FILTER (WHERE status = 'IN_PROGRESS') AS in_progress,
    COUNT(*) FILTER (WHERE status = 'CANCELLED')   AS cancelled,
    COUNT(*) FILTER (WHERE status = 'DISPUTED')    AS disputed,
    SUM(total_value)                  AS total_value_traded
FROM (
         SELECT offeror_id  AS user_id, status,
                COALESCE(offeror_crop_quantity, 0) AS total_value
         FROM barter_transactions
         UNION ALL
         SELECT acceptor_id AS user_id, status,
                COALESCE(acceptor_crop_quantity, 0) AS total_value
         FROM barter_transactions
     ) t
GROUP BY user_id
    WITH DATA;

CREATE UNIQUE INDEX ON mv_user_transaction_summary(user_id);

CREATE OR REPLACE VIEW v_transaction_delivery_progress AS
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
        END                                                           AS delivery_progress_pct
FROM crop_commitments cc
GROUP BY cc.transaction_id;
