# arch-cqrs-event-sourcing

Six small, isolated examples of CQRS and event sourcing on a tiny Orders domain: a read/write split, a hand-rolled Postgres event store, an Axon aggregate, projection rebuilds, snapshots and event upcasting.

## What is inside

| Folder | What it shows | Run |
| --- | --- | --- |
| [`cqrs-read-write-split`](./cqrs-read-write-split) | A command side that validates and emits events, and a read model fed only by those events | `mvn -q test` |
| [`event-store-handrolled`](./event-store-handrolled) | Append-only Postgres event table with optimistic concurrency on `(stream_id, version)` | `mvn -q test` (needs Docker) |
| [`axon`](./axon) | Axon Framework 4 event-sourced aggregate, tested with the fixture and a real command bus | `mvn -q test` |
| [`projections-rebuild`](./projections-rebuild) | A projection with its own checkpoint that catches up incrementally and rebuilds from zero | `mvn -q test` |
| [`snapshots`](./snapshots) | Loading state from the latest snapshot and counting how many events are replayed | `mvn -q test` |
| [`upcasting`](./upcasting) | Old stored events lifted to the current schema on read, without rewriting storage | `mvn -q test` |

Each folder is its own Maven project; run the command from inside the folder.

## Prerequisites

- Java 21
- Maven 3.8 or newer
- Docker running (only for `event-store-handrolled`, which starts `postgres:16.4-alpine` through Testcontainers)

## How to read it

Start with `cqrs-read-write-split` for the basic idea, then `event-store-handrolled` to see what an event store is underneath. The remaining folders each isolate one concern and can be read in any order. Only the event store uses real infrastructure; the others are in memory, and Axon runs with its in-memory storage engine. No Spring Boot is used.
