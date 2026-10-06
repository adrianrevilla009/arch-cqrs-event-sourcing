package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lab.Snapshots.Repo;
import org.junit.jupiter.api.Test;

class SnapshotsTest {
    @Test
    void snapshotGivesSameStateWithFewerReplays() {
        var repo = new Repo(10);
        for (int i = 1; i <= 25; i++) repo.append(i);
        var full = repo.load(false);
        var fast = repo.load(true);
        assertEquals(full.balance(), fast.balance());
        assertEquals(25, full.eventsReplayed());
        assertEquals(5, fast.eventsReplayed());
    }

    @Test
    void withoutEnoughEventsTheSnapshotIsNotUsed() {
        var repo = new Repo(10);
        for (int i = 0; i < 9; i++) repo.append(1);
        assertEquals(9, repo.load(true).eventsReplayed());
    }
}
