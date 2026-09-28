-- storing periodic checkpoints instead of one live number (V2) or full recalculation (V1)
-- each row says: "on this date, this account's balance was this amount"
CREATE TABLE account_balance_snapshots (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                           account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                                           snapshot_date DATE NOT NULL,
                                           balance NUMERIC(15,2) NOT NULL,
                                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                           UNIQUE (account_id, snapshot_date)
);

CREATE INDEX idx_snapshots_account_date ON account_balance_snapshots (account_id, snapshot_date);