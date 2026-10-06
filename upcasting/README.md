# upcasting

An `Upcaster` that lifts stored v1 `OrderPlaced` events to the v2 shape when they are read, using Jackson.

## Goal

Show how an event schema can change while old events stay untouched in storage: readers always get the current shape.

## Run it

```
mvn -q test
```

Expected: `UpcasterTest` passes (3 tests, 0 failures); `mvn -B test` ends with `BUILD SUCCESS`.

## What it proves

- In `Upcaster.java`, v1 `{orderId, amount}` (euros as a decimal) becomes v2 `{orderId, cents, currency}`. An amount of 12.5 reads as 1250 cents in `EUR`.
- A v2 event passes through unchanged, and the original `Stored` v1 record keeps its version and JSON bytes after reading.
- `readAll` over a mixed history of v1, v2 and an `OrderShipped` event returns two uniform `OrderPlacedV2` values and skips the other type.

## Trade-offs

- Upcasting runs on every read, so long chains of versions cost time; snapshots or occasional migrations can limit that.
- The v1 to `EUR` default is a guess that suits this example but may be wrong for real data.
- Only one step (v1 to v2) exists and only for `OrderPlaced`; the version chain is a simple `if`, not a registry.
- Unknown versions are not rejected; a missing field would fail with an `IllegalStateException`.

## When not to use it

- When you can afford a one-off migration of the stored events and have no need to keep old bytes intact.
- When the change is additive and a tolerant reader with default values covers it.
