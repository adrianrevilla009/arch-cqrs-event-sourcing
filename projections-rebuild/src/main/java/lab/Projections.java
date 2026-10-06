package lab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A log of events plus a projection that tracks its own position, so it can resume or be rebuilt from zero. */
public class Projections {
    public record Event(long seq, String customer, long cents) {}

    public static class Log {
        private final List<Event> events = new ArrayList<>();

        public Event append(String customer, long cents) {
            var e = new Event(events.size() + 1, customer, cents);
            events.add(e);
            return e;
        }

        public List<Event> after(long seq) { return events.subList((int) seq, events.size()); }
    }

    /** Spend per customer. Holds its checkpoint next to its data, as a real projection table would. */
    public static class SpendProjection {
        private Map<String, Long> spend = new HashMap<>();
        private long checkpoint = 0;

        public void catchUp(Log log) {
            for (Event e : log.after(checkpoint)) {
                spend.merge(e.customer(), e.cents(), Long::sum);
                checkpoint = e.seq();
            }
        }

        /** Drop all derived state and replay from the start; the new logic simply applies to history. */
        public void rebuild(Log log) {
            spend = new HashMap<>();
            checkpoint = 0;
            catchUp(log);
        }

        public long spend(String customer) { return spend.getOrDefault(customer, 0L); }
        public long checkpoint() { return checkpoint; }
    }
}
