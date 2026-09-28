-- this one's for dashboard-style questions, not single balance lookups
-- a normal view would just re-run the query every time; materialized means it's actually stored,
-- like a snapshot, until we tell it to refresh
CREATE MATERIALIZED VIEW monthly_category_spend AS
SELECT
    a.user_id,
    t.category_id,
    date_trunc('month', t.occurred_at) AS month,
    SUM(t.amount) AS total
FROM transactions t
    JOIN accounts a ON a.id = t.account_id
GROUP BY a.user_id, t.category_id, date_trunc('month', t.occurred_at);

-- needed so we can refresh it without locking reads (CONCURRENTLY requires a unique index)
CREATE UNIQUE INDEX idx_monthly_category_spend_unique
    ON monthly_category_spend (user_id, category_id, month);