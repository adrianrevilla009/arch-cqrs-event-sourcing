# cqrs-read-write-split

An in-memory Orders command side that emits events, and a revenue read model that is updated only by those events.

## Goal

Show the CQRS split: the write side holds just enough state to accept or reject commands, and the read side is a separate view shaped for one query.

## Run it

```
mvn -q test
```

Expected: `OrdersTest` passes (2 tests, 0 failures); `mvn -B test` ends with `BUILD SUCCESS`.

## What it proves

- `CommandSide` in `Orders.java` tracks only an order id and a shipped flag. `RevenueView` is subscribed to its events and is the only thing that knows customers and amounts.
- After two orders are placed for `ada` (500 and 700) shipped revenue is 0; after shipping `o2` it is 700. The view changes only when an event arrives.
- Invalid commands (amount 0, shipping an unknown order) throw and publish no event, so the view stays at 0. Duplicate and double-ship are also rejected in code.

## Trade-offs

- Events are delivered synchronously in the same call, so the read model is never stale here. A real system is eventually consistent, which this does not show.
- Events are not persisted; state is lost when the process ends. See `event-store-handrolled` for storage.
- Two models mean two shapes to keep in sync when the domain changes.

## When not to use it

- For simple CRUD where one table serves both reads and writes, the split adds code with no benefit.
- When reads must reflect a write immediately and there is no tolerance for lag, a single model is simpler.
