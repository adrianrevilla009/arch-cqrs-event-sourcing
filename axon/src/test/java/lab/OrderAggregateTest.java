package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lab.OrderAggregate.OrderPlaced;
import lab.OrderAggregate.OrderShipped;
import lab.OrderAggregate.PlaceOrder;
import lab.OrderAggregate.ShipOrder;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.Test;

class OrderAggregateTest {
    @Test
    void fixtureGivenWhenThen() {
        var fixture = new AggregateTestFixture<>(OrderAggregate.class);
        fixture.given(new OrderPlaced("o1", 500))
               .when(new ShipOrder("o1"))
               .expectEvents(new OrderShipped("o1"));
        fixture.given(new OrderPlaced("o1", 500), new OrderShipped("o1"))
               .when(new ShipOrder("o1"))
               .expectException(IllegalStateException.class);
    }

    @Test
    void realCommandBusAndEventStoreRehydrateTheAggregate() throws Exception {
        Configuration cfg = DefaultConfigurer.defaultConfiguration()
                .configureAggregate(OrderAggregate.class)
                .configureEmbeddedEventStore(c -> new InMemoryEventStorageEngine())
                .buildConfiguration();
        cfg.start();
        try {
            CommandGateway gw = cfg.commandGateway();
            gw.sendAndWait(new PlaceOrder("o1", 500));
            gw.sendAndWait(new ShipOrder("o1"));
            assertEquals(2, cfg.eventStore().readEvents("o1").asStream().count());
            assertThrows(IllegalStateException.class, () -> gw.sendAndWait(new ShipOrder("o1")));
        } finally {
            cfg.shutdown();
        }
    }
}
