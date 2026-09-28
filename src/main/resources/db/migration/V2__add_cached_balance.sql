-- adding a real column instead of calculating it every time
ALTER TABLE accounts ADD COLUMN balance NUMERIC(15,2) NOT NULL DEFAULT 0;

-- backfill existing accounts so balance matches what SUM() would've given us
UPDATE accounts a
SET balance = COALESCE((
                           SELECT SUM(amount) FROM transactions t WHERE t.account_id = a.id
                       ), 0);