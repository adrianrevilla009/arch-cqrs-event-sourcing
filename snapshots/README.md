# snapshots

An in-memory account repository that takes a snapshot every N events and can load state with or without it.

## Goal

Show that a snapshot gives the same state as a full replay while reading far fewer events.

## Run it

```
mvn -q test
```

Expected: `SnapshotsTest` passes (2 tests, 0 failures); `mvn -B test` ends with `BUILD SUCCESS`.

## What it proves

- With a snapshot every 10 events and 25 appended, `Repo.load(false)` in `Snapshots.java` replays 25 events and `load(true)` replays 5. Both return the same balance.
- The snapshot taken at event 20 stores version and balance, so loading only replays events 21 to 25.
- With 9 events and an interval of 10 no snapshot exists yet, so `load(true)` still replays all 9.

## Trade-offs

- Snapshots are a cache and can be rebuilt, but a changed event or state shape invalidates them and they need versioning.
- Here the snapshot is computed by a full replay inside `append`, which defeats the purpose in a real system; a real one would fold from the previous snapshot.
- Only the latest snapshot is kept, and it is held in memory, not stored.

## When not to use it

- For short streams, where replaying all events is already fast.
- Before measuring: snapshots add storage, invalidation and bugs, so add them only when load time is a real problem.
