# 4. CHECK constraints as a second line of defence

Date: 2026-07-23

Status: Accepted

## Context
Entities validate their invariants. That covers code going through
the domain layer.

It doesn't cover: a maintenance script running UPDATE directly, a
data migration, an older instance still deployed, a bulk import, or
me at 11pm fixing something "quickly" in psql.

For stock, a silent violation is overselling — expensive to detect
later and impossible to undo cleanly.

## Decision
Duplicate the invariants that matter as named CHECK constraints in
the migrations.

In the database:
- `available_quantity >= 0`
- `available_quantity <= total_quantity`
- `quantity > 0`
- `price >= 0`
- `ends_at > starts_at`
- `status IN (...)`

Not in the database: string lengths, blank checks, email format.

## Consequences
- The rule holds no matter what writes the row
- If my locking is wrong, I get a constraint violation instead of
  quiet corruption — a bug I can see
- Two places to keep in sync
- Constraint errors have terrible messages, so the domain layer has
  to catch things first for the API to stay usable

## Alternatives
Application-only — less duplication, but every non-application
write path is unguarded.

Database-only — every error becomes a constraint violation and the
API can't give useful messages.

## Criterion
Duplicate when a violation corrupts the domain in a way that's hard
to spot or undo. Blank text doesn't qualify. Negative stock does.