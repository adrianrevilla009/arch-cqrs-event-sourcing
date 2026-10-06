package lab;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

/** Append-only event table; the (stream_id, version) primary key is the optimistic-concurrency check. */
public class EventStore {
    public record StoredEvent(String streamId, int version, String type, String payload) {}

    public static class ConcurrencyException extends RuntimeException {
        public ConcurrencyException(String msg) { super(msg); }
    }

    private final DataSource ds;

    public EventStore(DataSource ds) { this.ds = ds; }

    public void init() {
        try (Connection c = ds.getConnection(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS events (
                  stream_id TEXT NOT NULL,
                  version   INT  NOT NULL,
                  type      TEXT NOT NULL,
                  payload   TEXT NOT NULL,
                  PRIMARY KEY (stream_id, version))""");
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    /** Appends events after expectedVersion (0 = new stream), all-or-nothing. */
    public void append(String streamId, int expectedVersion, List<StoredEvent> events) {
        try (Connection c = ds.getConnection()) {
            c.setAutoCommit(false);
            try (var ps = c.prepareStatement("INSERT INTO events (stream_id, version, type, payload) VALUES (?,?,?,?)")) {
                int v = expectedVersion;
                for (StoredEvent e : events) {
                    ps.setString(1, streamId);
                    ps.setInt(2, ++v);
                    ps.setString(3, e.type());
                    ps.setString(4, e.payload());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                if ("23505".equals(e.getSQLState()) || "23505".equals(nextState(e)))
                    throw new ConcurrencyException("stream " + streamId + " moved past version " + expectedVersion);
                throw e;
            }
        } catch (SQLException e) { throw new IllegalStateException(e); }
    }

    private static String nextState(SQLException e) {
        return e.getNextException() == null ? null : e.getNextException().getSQLState();
    }

    public List<StoredEvent> load(String streamId) {
        List<StoredEvent> out = new ArrayList<>();
        try (Connection c = ds.getConnection();
             var ps = c.prepareStatement("SELECT version, type, payload FROM events WHERE stream_id = ? ORDER BY version")) {
            ps.setString(1, streamId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) out.add(new StoredEvent(streamId, rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) { throw new IllegalStateException(e); }
        return out;
    }
}
