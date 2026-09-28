-- V1__init_schema.sql

-- ============================================================
-- BALANCE STRATEGY NOTE:
-- V1 uses calculated balances (SUM over transactions), backed by indexes.
-- Future migrations will optimize progressively as real usage grows:
--   V2: cached balance column + trigger/service-level update
--   V3: materialized view for reporting/dashboards
--   V4: balance snapshots (periodic checkpoints, e.g. daily/monthly)
--   V5: event-sourced ledger (full audit trail, balance = replay of events)
-- Each step is a deliberate learning milestone, not built all at once.
-- ============================================================

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       username VARCHAR(50) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE accounts (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          name VARCHAR(100) NOT NULL,
                          type VARCHAR(20) NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS', 'CASH', 'WALLET')),
                          base_currency VARCHAR(3) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            name VARCHAR(100) NOT NULL,
                            type VARCHAR(10) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE transactions (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                              category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
                              amount NUMERIC(15,2) NOT NULL,
                              currency VARCHAR(3) NOT NULL,
                              description VARCHAR(255),
                              occurred_at TIMESTAMPTZ NOT NULL,
                              transfer_group_id UUID,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Indexes: the balance calculation is SUM(amount) WHERE account_id = ?
-- This index makes that scan fast even as transactions grow into the thousands.
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_occurred_at ON transactions(occurred_at);
CREATE INDEX idx_transactions_account_occurred ON transactions(account_id, occurred_at);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_categories_user_id ON categories(user_id);