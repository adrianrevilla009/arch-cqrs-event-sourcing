package lab;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/** Event-sourced aggregate: command handlers decide and apply events, event handlers rebuild state. */
public class OrderAggregate {
    public record PlaceOrder(@TargetAggregateIdentifier String orderId, long cents) {}
    public record ShipOrder(@TargetAggregateIdentifier String orderId) {}
    public record OrderPlaced(String orderId, long cents) {}
    public record OrderShipped(String orderId) {}

    @AggregateIdentifier
    private String orderId;
    private boolean shipped;

    protected OrderAggregate() {}

    @CommandHandler
    public OrderAggregate(PlaceOrder c) {
        if (c.cents() <= 0) throw new IllegalArgumentException("amount must be positive");
        apply(new OrderPlaced(c.orderId(), c.cents()));
    }

    @CommandHandler
    public void handle(ShipOrder c) {
        if (shipped) throw new IllegalStateException("already shipped");
        apply(new OrderShipped(c.orderId()));
    }

    @EventSourcingHandler
    void on(OrderPlaced e) { this.orderId = e.orderId(); }

    @EventSourcingHandler
    void on(OrderShipped e) { this.shipped = true; }
}
