-- from here on, events are the truth. nothing gets updated or deleted, only appended.
-- a mistake gets fixed by adding a CORRECTION event, never by editing an old row.
CREATE TABLE account_events (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                account_id UUID NOT NULL REFERENCES accounts(id),
    -- position of this event inside its account's history (1, 2, 3...)
                                sequence_no BIGINT NOT NULL,
                                event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('CREDIT', 'DEBIT', 'CORRECTION')),
    -- signed like before: positive = money in, negative = money out
                                amount NUMERIC(15,2) NOT NULL,
                                currency VARCHAR(3) NOT NULL,
                                category_id UUID REFERENCES categories(id),
                                description VARCHAR(255),
                                transfer_group_id UUID,
    -- extra stuff that doesn't deserve its own column (source PDF, import id, etc)
                                payload JSONB,
    -- when it happened in real life vs when we wrote it down
                                occurred_at TIMESTAMPTZ NOT NULL,
                                recorded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- two writers can't both claim the same spot in an account's history
                                UNIQUE (account_id, sequence_no)
);

CREATE INDEX idx_events_account_occurred ON account_events (account_id, occurred_at);

-- move the old transactions in, numbering them per account in chronological order
INSERT INTO account_events
(id, account_id, sequence_no, event_type, amount, currency, category_id,
 description, transfer_group_id, occurred_at, recorded_at)
SELECT
    id,
    account_id,
    ROW_NUMBER() OVER (PARTITION BY account_id ORDER BY occurred_at, id),
    CASE WHEN amount >= 0 THEN 'CREDIT' ELSE 'DEBIT' END,
    amount, currency, category_id, description, transfer_group_id, occurred_at, created_at
FROM transactions;

-- safety net: if the copy lost anything, blow up before we drop the old table
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM account_events) <> (SELECT COUNT(*) FROM transactions) THEN
        RAISE EXCEPTION 'backfill mismatch, not dropping transactions';
END IF;
END $$;

-- the database itself refuses to edit history, not just our code
CREATE FUNCTION forbid_event_changes() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'account_events is append-only, add a CORRECTION event instead';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER account_events_immutable
    BEFORE UPDATE OR DELETE ON account_events
FOR EACH ROW EXECUTE FUNCTION forbid_event_changes();

-- the V3 view reads from transactions, so it has to go first
DROP MATERIALIZED VIEW monthly_category_spend;
DROP TABLE transactions;

-- same view as before, now built on events
CREATE MATERIALIZED VIEW monthly_category_spend AS
SELECT
    a.user_id,
    e.category_id,
    date_trunc('month', e.occurred_at) AS month,
    SUM(e.amount) AS total
FROM account_events e
    JOIN accounts a ON a.id = e.account_id
GROUP BY a.user_id, e.category_id, date_trunc('month', e.occurred_at);

CREATE UNIQUE INDEX idx_monthly_category_spend_unique
    ON monthly_category_spend (user_id, category_id, month);