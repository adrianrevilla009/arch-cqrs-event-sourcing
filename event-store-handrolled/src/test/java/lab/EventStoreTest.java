package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import lab.EventStore.ConcurrencyException;
import lab.EventStore.StoredEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

class EventStoreTest {
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:16.4-alpine");
    static EventStore store;

    @BeforeAll
    static void start() {
        PG.start();
        var ds = new PGSimpleDataSource();
        ds.setUrl(PG.getJdbcUrl());
        ds.setUser(PG.getUsername());
        ds.setPassword(PG.getPassword());
        store = new EventStore(ds);
        store.init();
    }

    static StoredEvent ev(String type) { return new StoredEvent("o1", 0, type, "{}"); }

    @Test
    void appendAndLoadInOrder() {
        store.append("a", 0, List.of(ev("Placed"), ev("Shipped")));
        var events = store.load("a");
        assertEquals(List.of(1, 2), events.stream().map(StoredEvent::version).toList());
        assertEquals("Shipped", events.get(1).type());
    }

    @Test
    void staleWriterIsRejectedAndNothingPartialIsStored() {
        store.append("b", 0, List.of(ev("Placed")));
        assertThrows(ConcurrencyException.class, () -> store.append("b", 0, List.of(ev("Shipped"), ev("Shipped"))));
        assertEquals(1, store.load("b").size());
        store.append("b", 1, List.of(ev("Shipped")));
        assertEquals(2, store.load("b").size());
    }
}
