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
