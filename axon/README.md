# axon

An Axon Framework 4.10.3 event-sourced `OrderAggregate`, tested with the Axon fixture and with a real command bus over an in-memory event store.

## Goal

Show how Axon turns the hand-written pieces of event sourcing (decide, emit, rebuild state) into annotated handlers on one aggregate.

## Run it

```
mvn -q test
```

Expected: `OrderAggregateTest` passes (2 tests, 0 failures); `mvn -B test` ends with `BUILD SUCCESS`.

## What it proves

- In `OrderAggregate.java`, `@CommandHandler` methods validate and call `apply(...)`, while `@EventSourcingHandler` methods are the only place state (`orderId`, `shipped`) changes.
- The fixture test gives `OrderPlaced`, sends `ShipOrder` and expects `OrderShipped`; with `OrderShipped` already in history it expects `IllegalStateException`.
- The second test configures a real `CommandGateway` with `InMemoryEventStorageEngine`: place then ship stores 2 events for `o1`, and a second ship is rejected because the aggregate was rehydrated from them.

## Trade-offs

- Storage is in memory, so nothing survives a restart. No Axon Server, database, event processors or read models are involved.
- The aggregate has no snapshot configuration and no upcasters.
- The annotation model hides the plumbing, which is convenient but harder to debug than the explicit code in the other folders.
- `PlaceOrder` rejects non-positive amounts, but this is not covered by a test here.

## When not to use it

- For a small service where a hand-rolled store, as in `event-store-handrolled`, is enough and you want no framework dependency.
- When you cannot commit to Axon's conventions and its upgrade path between major versions.
