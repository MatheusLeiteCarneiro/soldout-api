# 2. Behaviour on entities instead of setters

Date: 2026-07-23

Status: Accepted

## Context
The whole project rests on one rule: `availableQuantity` never goes
negative.

With a public setter, that rule is a suggestion:

    ticketType.setAvailableQuantity(9999);

Any service, any test, any future code path can do that and the
compiler won't say a word. Putting the check in the service layer
only helps callers who go through the service.

## Decision
No public setters. State changes go through methods that say what
they mean — `publish()`, `reschedule()`, `reserve()` — and each one
validates the transition.

Constructor is the only way in, and it rejects invalid input.
The Hibernate no-args constructor is protected.

## Consequences
- Stock has exactly one mutation path, which is a precondition for
  any locking strategy to actually work
- PATCH needs intent-specific endpoints instead of generic field
  updates. `/events/{id}/publish` reads better
- More code per entity

## Alternatives
Anemic entities with logic in services. This is what most Spring
tutorials teach and what I did on the previous project. Rejected
because it leaves the one rule this project exists to protect
completely unguarded.

## Note
I moved format validation (max length, blank) out of the entity
and into the DTOs. Entities got noticeably heavy and those
rules aren't domain invariants — a 200-char name doesn't corrupt
anything, it just doesn't fit the column. Entities keep period
ordering, status transitions and initial state.