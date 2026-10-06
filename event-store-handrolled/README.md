# event-store-handrolled

A small `EventStore` class over a Postgres `events` table, tested against a real Postgres started by Testcontainers.

## Goal

Show what an event store needs at minimum: append-only storage per stream, ordered reads, and a conflict check so two writers cannot both extend the same version.

## Run it

```
mvn -q test
```

Docker must be running. Expected: `EventStoreTest` passes (2 tests, 0 failures) after pulling and starting `postgres:16.4-alpine`; it took about 10 seconds here once the image was cached.

## What it proves

- `append` in `EventStore.java` assigns versions `expectedVersion + 1, +2, ...` in one transaction, and `load` returns them ordered (versions 1 and 2 for a two-event append).
- The primary key `(stream_id, version)` is the optimistic-concurrency check. A writer that still expects version 0 on a stream already at version 1 gets a `ConcurrencyException`.
- The rejected append stores nothing, even though it carried two events: the stream stays at one event, and a retry with expected version 1 succeeds.

## Trade-offs

- There is no global ordering column and no subscription or polling; projections would need to read per stream.
- Payloads are plain text strings; there is no serialization, schema registry or upcasting here (see `upcasting`).
- Detecting a conflict relies on SQL state `23505` (unique violation), which ties the class to Postgres.
- `init()` creates the table inline; there is no migration tooling.

## When not to use it

- In production, where a maintained store (Axon Server, EventStoreDB, a tested library) handles ordering, subscriptions and retention.
- When Docker is not available, since the test cannot start without it.
