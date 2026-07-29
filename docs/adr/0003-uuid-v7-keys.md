# 3. UUID v7 generated in the constructor

Date: 2026-07-23

Status: Accepted

## Context
Started with `@GeneratedValue(strategy = UUID)`. Hit the problem when
thinking about how to build a `TicketType` that references an `Event`
not yet persisted — the id is still null at that point. Same reason
`hashCode` changes after save, which breaks entities kept in a Set.

Then found the second issue: random UUIDs fragment B-tree indexes.
Sequential keys always land on the last page, which stays cached.
Random keys hit unpredictable pages, causing more I/O and more page
splits.

UUID v7 puts a millisecond timestamp in the high bits, so it stays
ordered.

## Decision
Generate v7 in constructors through `IdGenerator`, backed by
`com.github.f4b6a3:uuid-creator`.

`Ticket.code` stays v4 — that one is a credential, not an
identifier, and v7 would leak the issue time along with it.

## Consequences
- Id exists from construction: aggregates can be built in memory,
  equals/hashCode stay stable, retries can be idempotent
- Index locality close to sequential
- External dependency until the JDK ships v7
- 16 bytes instead of 8
- The id tells you when the row was created
- Spring Data's `isNew()` checks whether the id is null. With an
  assigned id it always returns false, so `save()` runs `merge()` and
  fires a SELECT before every INSERT. Fixed with a `@MappedSuperclass`
  implementing `Persistable` and flipping a transient flag on
  `@PostPersist` / `@PostLoad`.

## Alternatives
BIGSERIAL — smallest and fastest, but leaks how many records exist
(anyone with id=47 knows there are 47 events) and still isn't
available before insert.

`@GeneratedValue(strategy = GenerationType.UUID)` — worst of both: generated late *and*
random, so it fragments the index too.

Custom Hibernate generator producing v7 — would fix the ordering
but keep the late-availability problem, which was the reason I
started looking.

## Note
Wrapped in `IdGenerator` so this is one line to change when the JDK
catches up.