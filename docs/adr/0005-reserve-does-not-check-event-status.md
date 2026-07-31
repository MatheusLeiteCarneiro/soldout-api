# 5. Reserve does not check event status

Date: 2026-07-24
Status: Accepted

## Context
Define where the event status must be validated before reserving tickets.
Only PUBLISHED events should accept reservations.

- To follow the ADR 0002 decision, my first thought was to delegate the
  reservation method to the Event class. But looking at the @OneToMany
  relationship with TicketType, this would load the whole event plus all
  its ticket types just to decrement one row.
- The second option was checking `event.getStatus()` inside
  `TicketType.reserve()`. The relationship is LAZY, so this fires an
  extra query in the middle of the reservation.
- That matters because `reserve()` is the hot path of the system. Every
  query inside it extends the window where the stock row is contended.
  I want the least possible work in there.

## Decision
Delegate the responsibility of validating the event status to the service,
even if this contradicts the ADR 0002 decision to make the entities
responsible for their own validations.

- The service loads TicketType with its Event in a single JOIN FETCH.
- In the Event entity, `ensureOpenForReservation()` owns the rule and its
  messages; the service only decides when to call it.
- `reserve()` only validates what it owns: positive quantity and
  sufficient stock.

## Consequences
- One query instead of two, and I decide when it happens.
- Nothing extra is loaded inside the transaction holding the contended row.
- If used in another code, verification of the event status is necessary
  before reserving tickets. The rule is not enforced by the compiler.

## When to revisit
The repositories are already package-private, so the service is the only
entry point. If a third method needs this treatment, the exception stops
being an exception and ADR 0002 should be rewritten to say where status
validation belongs.

## Note
`TicketTypeService.update` follows the same shape, for a different reason.

Routing it through the aggregate root means loading the Event and walking
`ticketTypes` to find the one being edited — a second query that hydrates
every sibling ticket type just to change one. The service loads the
TicketType with its Event in a single JOIN FETCH, calls
`event.ensureModifiable()`, and lets `updateDetails()` validate only the
price.

The hot path argument above does not apply here. `update` is not
contended; what is being avoided is loading N rows to modify one.

Both paths share the same shape: `ensureModifiable()` and
`ensureOpenForReservation()` are public on Event, each owning its rule and
its message, and the service invokes them instead of reimplementing the
check.

## Update

2026-07-31 — `isPublished()` was replaced by `ensureOpenForReservation()`.

The original decision left the two exceptions to ADR 0002 in different
shapes: `update` called `ensureModifiable()` on the entity, while `reserve`
asked `isPublished()` and then wrote its own check and its own message in
the service. Adding a second rule — reservations are refused once `endsAt`
has passed — meant the service would hold two pieces of domain logic and
two message strings. That is the drift this ADR exists to bound.

Moving both rules into `ensureOpenForReservation()` preserves what this ADR
actually decided. The service still chooses *when* the check happens, so
nothing extra is loaded inside the transaction holding the contended row,
and `reserve()` still validates only quantity and stock. What changed is
that the entity owns *what* the rule is.

The exception to ADR 0002 is now about invocation order, not about where
domain rules live — a narrower exception than the one first recorded here.