package lab;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Loads an account by replaying events, starting from the latest snapshot when one exists. */
public class Snapshots {
    public record Event(int version, long delta) {}
    public record Snapshot(int version, long balance) {}
    public record Loaded(long balance, int version, int eventsReplayed) {}

    public static class Repo {
        private final List<Event> events = new ArrayList<>();
        private Snapshot snapshot;
        private final int every;

        public Repo(int snapshotEvery) { this.every = snapshotEvery; }

        public void append(long delta) {
            events.add(new Event(events.size() + 1, delta));
            if (events.size() % every == 0) snapshot = new Snapshot(events.size(), load(false).balance());
        }

        public Loaded load(boolean useSnapshot) {
            Optional<Snapshot> s = useSnapshot ? Optional.ofNullable(snapshot) : Optional.empty();
            long balance = s.map(Snapshot::balance).orElse(0L);
            int from = s.map(Snapshot::version).orElse(0);
            int replayed = 0;
            for (Event e : events.subList(from, events.size())) {
                balance += e.delta();
                replayed++;
            }
            return new Loaded(balance, events.size(), replayed);
        }
    }
}
