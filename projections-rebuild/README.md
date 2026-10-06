# projections-rebuild

An in-memory event log and a spend-per-customer projection that keeps its own checkpoint, so it can resume or be rebuilt.

## Goal

Show that a projection is disposable: it can catch up incrementally from where it stopped, or be dropped and replayed from the start of the log.

## Run it

```
mvn -q test
```

Expected: `ProjectionsTest` passes (2 tests, 0 failures); `mvn -B test` ends with `BUILD SUCCESS`.

## What it proves

- `SpendProjection.catchUp` in `Projections.java` reads only events after its checkpoint. Calling it twice with no new events leaves `ada` at 100, and after a second event of 50 she is at 150 with checkpoint 2.
- `rebuild` resets the map and checkpoint to zero and replays the whole `Log`. A projection rebuilt this way matches one built fresh from the same log.
- The checkpoint lives with the projection data, which is how a real projection table avoids applying an event twice.

## Trade-offs

- The log and projection are in memory, and the checkpoint is not saved atomically with the data in any store, so crash safety is not demonstrated.
- Rebuild replays everything; on a large log it is slow and needs a plan for serving reads meanwhile.
- The test "repairs" a projection by rebuilding, but it never actually corrupts it first, so that claim rests on the reset logic only.

## When not to use it

- When the projection is cheap to keep consistent in the same transaction as the write, a rebuild mechanism is unnecessary.
- For very large logs, where you need parallel or blue-green rebuilds rather than a single in-process replay.
