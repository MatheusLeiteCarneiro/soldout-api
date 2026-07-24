# 1. Capacity-based inventory, no assigned seating

Date: 2026-07-23

Status: Accepted

## Context
Two ways to model ticket stock:

Assigned seating pre-creates one row per seat. Selling means claiming
a specific row, and a UNIQUE constraint makes double-selling
impossible — the database does the work.

Capacity stores a single number that decrements. Nothing in the
schema prevents overselling, and every buyer contends for the same
row.

I want the second one. It's the harder case: maximum contention,
zero free guarantees.

## Decision
Capacity only. Stock is `TicketType.availableQuantity`.

## Consequences
- The invariant is entirely my responsibility
- One row becomes the contention point for the whole system, which
  is exactly the scenario worth demonstrating
- No seat selection, no seat map

## Alternatives
Assigned seating — easier to get right, which is why I'm not doing it.

Supporting both — would need JPA inheritance and a lot of branching
for something that adds nothing to the concurrency problem.
