package lab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Write side validates commands and emits events; read side is a denormalised view fed only by events. */
public class Orders {
    public sealed interface Event permits Placed, Shipped {}
    public record Placed(String orderId, String customer, long cents) implements Event {}
    public record Shipped(String orderId) implements Event {}

    public record PlaceOrder(String orderId, String customer, long cents) {}
    public record ShipOrder(String orderId) {}

    /** Write model: only what is needed to decide. */
    public static class CommandSide {
        private final Map<String, Boolean> shipped = new HashMap<>();
        private final List<Consumer<Event>> subscribers = new ArrayList<>();

        public void subscribe(Consumer<Event> s) { subscribers.add(s); }

        public void handle(PlaceOrder c) {
            if (c.cents() <= 0) throw new IllegalArgumentException("amount must be positive");
            if (shipped.containsKey(c.orderId())) throw new IllegalStateException("duplicate order");
            shipped.put(c.orderId(), false);
            publish(new Placed(c.orderId(), c.customer(), c.cents()));
        }

        public void handle(ShipOrder c) {
            Boolean s = shipped.get(c.orderId());
            if (s == null) throw new IllegalStateException("unknown order");
            if (s) throw new IllegalStateException("already shipped");
            shipped.put(c.orderId(), true);
            publish(new Shipped(c.orderId()));
        }

        private void publish(Event e) { subscribers.forEach(s -> s.accept(e)); }
    }

    /** Read model: shipped revenue per customer, shaped for one query. No business rules here. */
    public static class RevenueView {
        private final Map<String, String> customerOf = new HashMap<>();
        private final Map<String, Long> amountOf = new HashMap<>();
        private final Map<String, Long> shippedRevenue = new HashMap<>();

        public void on(Event e) {
            switch (e) {
                case Placed p -> { customerOf.put(p.orderId(), p.customer()); amountOf.put(p.orderId(), p.cents()); }
                case Shipped s -> shippedRevenue.merge(customerOf.get(s.orderId()), amountOf.get(s.orderId()), Long::sum);
            }
        }

        public long shippedRevenue(String customer) { return shippedRevenue.getOrDefault(customer, 0L); }
    }
}
