# Finance App

A personal finance backend built as a learning project — Spring Boot, event-sourced ledger, JWT auth, PDF import, with local/offline AI features in progress.

This isn't just a CRUD app. Each core design decision (balance calculation, auth, PDF parsing) was deliberately built through multiple iterations to learn the trade-offs, not just to ship the "final" version. See [Design Journey](#design-journey) below.

## Stack

- **Java 21**, Spring Boot 4
- **PostgreSQL** with Flyway migrations
- **Spring Security** + JWT (stateless)
- **Event sourcing**: an append-only ledger (`account_events`) is the single source of truth, not a mutable transactions table
- **PDFBox** for statement import
- **Spring AI + Ollama** (in progress) for local, offline AI features — no cloud LLM calls
- **Docker Compose** for local Postgres + app
- **GitHub Actions** CI (build + test on every push)

## Why event sourcing?

Balances aren't stored as a mutable number. Every credit/debit is an immutable, append-only event; the balance is a fold over the event stream. This gives:
- A full audit trail for free (nothing is ever edited or deleted, only appended)
- Mistakes are fixed with a `CORRECTION` event, not silent edits
- A cached balance column (kept in sync in the same DB transaction) makes reads fast without giving up the audit trail

## Features so far

- **Auth**: signup → admin manually enables the account (no email verification, by design, since the app stays offline) → JWT login
- **Accounts & categories**: per-user, created via API
- **Event ledger**: append events (`CREDIT`/`DEBIT`/`CORRECTION`), paginated history, cached balance, balance-consistency check (replay vs. cache)
- **PDF import**: two-phase — upload a statement, review the parsed transactions (dates/amounts/descriptions), then confirm only the ones that are correct. Nothing is written to the ledger without review.
- **Ownership enforcement**: every account-scoped endpoint verifies the JWT caller owns the resource

## Design journey

Documented here rather than buried in commit history, since the *why* mattered more than the *what* for this project:

- **V1**: balance calculated on the fly (`SUM()` over events), backed by indexes
- **V2**: cached `balance` column on `accounts`, updated in the same transaction as each event — ~120x faster reads, verified with `EXPLAIN ANALYZE` at 500k rows
- **V3**: materialized view for monthly spend-by-category reporting — ~4x faster than a live aggregation, once warmed and analyzed
- **V4**: balance snapshots (periodic checkpoints) to bound replay cost regardless of history length
- **V5**: replaced the mutable `transactions` table entirely with an append-only `account_events` table, enforced immutable at the DB level via a trigger

Each step was benchmarked against realistic seeded data (hundreds of thousands of events across 200 accounts) before moving to the next, specifically to learn *when* each optimization actually earns its complexity, not just to check a box.

## Running locally

```bash
docker compose up --build
```

Or without Docker, with a local Postgres running:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Seed realistic fake data (200 accounts, 500k events):

```bash
./gradlew bootRun --args='--spring.profiles.active=local,seed'
```

## Roadmap

- [ ] Ollama + Spring AI wiring (local model, no cloud calls)
- [ ] AI-assisted transaction categorization
- [ ] Natural-language Q&A over spending data
- [ ] pgvector + RAG for fuzzy search over transaction/PDF text
- [ ] Frontend

## License

Personal project, not licensed for reuse yet.