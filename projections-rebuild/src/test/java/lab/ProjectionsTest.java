package lab;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lab.Projections.Log;
import lab.Projections.SpendProjection;
import org.junit.jupiter.api.Test;

class ProjectionsTest {
    @Test
    void catchUpIsIncrementalAndIdempotent() {
        var log = new Log();
        var p = new SpendProjection();
        log.append("ada", 100);
        p.catchUp(log);
        p.catchUp(log);
        assertEquals(100, p.spend("ada"));
        log.append("ada", 50);
        p.catchUp(log);
        assertEquals(150, p.spend("ada"));
        assertEquals(2, p.checkpoint());
    }

    @Test
    void rebuildRepairsCorruptedProjection() {
        var log = new Log();
        var p = new SpendProjection();
        log.append("ada", 100);
        log.append("bob", 70);
        p.catchUp(log);
        var fresh = new SpendProjection();
        fresh.rebuild(log);
        p.rebuild(log);
        assertEquals(fresh.spend("ada"), p.spend("ada"));
        assertEquals(70, p.spend("bob"));
        assertEquals(2, p.checkpoint());
    }
}
