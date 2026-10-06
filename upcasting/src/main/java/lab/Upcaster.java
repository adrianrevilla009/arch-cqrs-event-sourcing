package lab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;

/**
 * Stored events are never rewritten. Old versions are lifted to the current shape when read:
 * v1 OrderPlaced {orderId, amount (euros)} -> v2 {orderId, cents, currency}.
 */
public class Upcaster {
    public record Stored(String type, int version, String json) {}
    public record OrderPlacedV2(String orderId, long cents, String currency) {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Stored upcast(Stored s) {
        if (!s.type().equals("OrderPlaced")) return s;
        Stored cur = s;
        if (cur.version() == 1) cur = v1ToV2(cur);
        return cur;
    }

    private static Stored v1ToV2(Stored s) {
        try {
            ObjectNode n = (ObjectNode) MAPPER.readTree(s.json());
            n.put("cents", Math.round(n.get("amount").asDouble() * 100));
            n.remove("amount");
            n.put("currency", "EUR");
            return new Stored(s.type(), 2, MAPPER.writeValueAsString(n));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public static OrderPlacedV2 read(Stored s) {
        Stored cur = upcast(s);
        try {
            JsonNode n = MAPPER.readTree(cur.json());
            return new OrderPlacedV2(n.get("orderId").asText(), n.get("cents").asLong(), n.get("currency").asText());
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public static List<OrderPlacedV2> readAll(List<Stored> history) {
        return history.stream().filter(s -> s.type().equals("OrderPlaced")).map(Upcaster::read).toList();
    }
}
