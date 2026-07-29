# 6. Pessimistic locking for ticket reservation

Date: 2026-07-27
Status: Accepted

## Context
The concurrency test proved that `TicketType.reserve()` alone does not
prevent overselling.

Observed with 50 concurrent buyers competing for 1 available ticket:

- 7 successful reservations
- 43 NotEnoughTicketsException
- final available_quantity: 0
- the number of successes changes on every run

What happens: all threads read `available_quantity = 1` before any of
them writes. Each one passes the validation, computes `1 - 1 = 0` and
sends `UPDATE ticket_types SET available_quantity = 0`.

- The CHECK constraint never fires. Zero is a valid value, so every
  individual write is legitimate. The constraint protects against an
  invalid value, not against a sequence of operations built on a stale
  read. This is the difference between data integrity and correct
  application behavior.
- The database ended up consistent. The system still sold 7 tickets
  that did not exist.

## Decision
Read the TicketType with a pessimistic write lock inside the reservation
transaction.

    SELECT ... FROM ticket_types WHERE id = ? FOR UPDATE

In Spring Data this is a `@Lock(LockModeType.PESSIMISTIC_WRITE)` on a
repository method used only by the reservation path.

The first transaction locks the row. The others block on the SELECT —
they never read the stale value. When the first one commits, the next is
released and reads the updated quantity, so `reserve()` throws
NotEnoughTicketsException on its own.

- No retry logic. The domain exception is the only one the client sees.
- The entity stays unchanged. Concurrency control lives in the query,
  not in the model (ADR 0002 stays intact).
- A lock timeout is configured so a slow transaction cannot block the
  queue indefinitely.

## Consequences
- Reservations for the same ticket type are serialized. Correctness is
  guaranteed by construction, not detected after the fact.
- Simpler service: no retry counter, no locking exception leaking into
  the domain.
- Every waiting thread holds a database connection. With a pool of 10
  and heavy contention, the 11th request cannot even start.
- Latency grows linearly with the queue. The last buyer in a line of
  1000 waits for all the others.
- Deadlock becomes possible once a single transaction locks more than
  one row — not an issue today, but it will be when Order reserves
  several ticket types at once.

## Alternatives considered
Optimistic locking with `@Version`. No row is locked; the conflict is
detected at write time, when the version no longer matches. Needs retry
logic in the service and a cap on attempts.

- Rejected because it fits the opposite scenario. Optimistic locking
  assumes conflicts are rare; in ticket sales for a popular event,
  conflict is the normal case. Under this load most attempts would
  collide, retry, and put load on the database for work that gets
  discarded.

Atomic conditional update:

    UPDATE ticket_types
       SET available_quantity = available_quantity - :qty
     WHERE id = :id AND available_quantity >= :qty

One statement, no lock, no retry, and the affected row count tells
whether it worked. This is probably the fastest option under contention.
Rejected because it moves the invariant out of the entity and into a
query, contradicting ADR 0002.

SERIALIZABLE isolation. Strongest guarantee, highest cost, and it still
needs retry on serialization failure.

## Beyond this scope
At scale, ticketing systems reduce contention before it reaches the
database: virtual waiting rooms, atomic counters in Redis, or splitting
the stock across partitions.

A virtual waiting room and a pessimistic lock solve the same problem —
serializing access — at different layers. The waiting room controls
admission before any transaction starts; the lock controls it after one
already began. They are complementary: reducing contention does not
remove the need for a correctness guarantee at the data level.

Those layers are out of scope here. This project keeps the problem where
the guarantee ultimately lives.

## When to revisit
If latency under load becomes the dominant problem rather than
correctness, the atomic conditional update is the next step.