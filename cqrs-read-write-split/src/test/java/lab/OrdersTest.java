package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lab.Orders.CommandSide;
import lab.Orders.PlaceOrder;
import lab.Orders.RevenueView;
import lab.Orders.ShipOrder;
import org.junit.jupiter.api.Test;

class OrdersTest {
    @Test
    void readModelIsUpdatedOnlyThroughEvents() {
        var cmd = new CommandSide();
        var view = new RevenueView();
        cmd.subscribe(view::on);
        cmd.handle(new PlaceOrder("o1", "ada", 500));
        cmd.handle(new PlaceOrder("o2", "ada", 700));
        assertEquals(0, view.shippedRevenue("ada"));
        cmd.handle(new ShipOrder("o2"));
        assertEquals(700, view.shippedRevenue("ada"));
    }

    @Test
    void invalidCommandsEmitNoEvents() {
        var cmd = new CommandSide();
        var view = new RevenueView();
        cmd.subscribe(view::on);
        assertThrows(IllegalArgumentException.class, () -> cmd.handle(new PlaceOrder("o1", "ada", 0)));
        assertThrows(IllegalStateException.class, () -> cmd.handle(new ShipOrder("nope")));
        assertEquals(0, view.shippedRevenue("ada"));
    }
}
