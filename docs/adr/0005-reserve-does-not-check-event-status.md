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
- In the Event entity, the method `isPublished()` was created to verify
  if the event is published.
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

One difference worth keeping: `ensureModifiable()` is public on Event and
owns both the rule and the message, so the service invokes it instead of
reimplementing it. `isPublished()` is only a question, and the service
still writes the `reserve()` check around it.