package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import lab.Upcaster.OrderPlacedV2;
import lab.Upcaster.Stored;
import org.junit.jupiter.api.Test;

class UpcasterTest {
    static final Stored OLD = new Stored("OrderPlaced", 1, "{\"orderId\":\"o1\",\"amount\":12.5}");
    static final Stored NEW = new Stored("OrderPlaced", 2, "{\"orderId\":\"o2\",\"cents\":900,\"currency\":\"USD\"}");

    @Test
    void v1EventsAreLiftedToV2OnRead() {
        assertEquals(new OrderPlacedV2("o1", 1250, "EUR"), Upcaster.read(OLD));
    }

    @Test
    void currentEventsPassThroughAndStoredBytesAreUntouched() {
        assertEquals(new OrderPlacedV2("o2", 900, "USD"), Upcaster.read(NEW));
        assertEquals(1, OLD.version());
        assertEquals("{\"orderId\":\"o1\",\"amount\":12.5}", OLD.json());
    }

    @Test
    void mixedHistoryReadsUniformly() {
        var all = Upcaster.readAll(List.of(OLD, NEW, new Stored("OrderShipped", 1, "{}")));
        assertEquals(2, all.size());
        assertEquals("EUR", all.get(0).currency());
    }
}
