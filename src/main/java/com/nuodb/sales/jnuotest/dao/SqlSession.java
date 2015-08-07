package com.nuodb.sales.jnuotest.dao;


import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;

/**
 * Created by nik on 7/5/15.
 */
public class SqlSession implements AutoCloseable {

    private final Mode mode;
    private final Mode commitMode;

    private Connection connection;
    private PreparedStatement batch;
    private List<PreparedStatement> statements;

    private static DataSource dataSource;
    private static ThreadLocal<SqlSession> current = new ThreadLocal<SqlSession>();
    private static Map<SqlSession, String> sessions;

    private static Logger log = Logger.getLogger("SqlSession");

    //private SqlSession(Mode mode) {
    //    this.mode = mode;
    //}

    public enum Mode { AUTO_COMMIT, TRANSACTIONAL, BATCH, READ_ONLY };

    public static void init(DataSource ds, int maxThreads) {
        dataSource = ds;
        sessions = new ConcurrentHashMap<SqlSession, String>(maxThreads, 0.85f, 256);
    }

    public SqlSession(Mode mode) {
        this.mode = mode;
        commitMode = (mode == Mode.AUTO_COMMIT || mode == Mode.READ_ONLY ? Mode.AUTO_COMMIT : Mode.TRANSACTIONAL);

        SqlSession session = current.get();
        if (session != null) {
            session.close();
            throw new PersistenceException("Previous session for this thread was not correctly closed");
        }

        //session = new SqlSession(mode);
        current.set(this);
        sessions.put(this, Thread.currentThread().getName());

        //return session;
    }

    public static void cleanup() {
        if (sessions.size() == 0)
            return;

        int released = 0;
        for (Map.Entry<SqlSession, String> entry : sessions.entrySet()) {
            log.info(String.format("cleaning up unclosed session from %s", entry.getValue()));
            entry.getKey().close();
            released++;
        }

        throw new PersistenceException("%d unclosed SqlSessions were cleaned up", released);
    }

    public static SqlSession getCurrent() {
        SqlSession session = current.get();
        if (session == null)
            throw new PersistenceException("No current session");

        return session;
    }

    public void rollback() {
        if (connection != null && commitMode != Mode.AUTO_COMMIT) {
            try { connection.rollback(); }
            catch (SQLException e) {}
        }
    }

    @Override
    public void close() {
        closeStatements();
        closeConnection();
        current.set(null);
        sessions.remove(this);
    }

    public PreparedStatement getStatement(String sql) throws SQLException {
        if (mode == Mode.BATCH && batch != null) {
            batch.clearParameters();
            return batch;
        }

        if (statements == null) {
            statements = new ArrayList<PreparedStatement>(16);
            //statements = new HashMap<String, PreparedStatement>(16);
        }

        //PreparedStatement ps = statements.get(sql);

        //if (ps == null) {
            int returnMode = (commitMode == Mode.AUTO_COMMIT ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
            //int returnMode = Statement.RETURN_GENERATED_KEYS;
            PreparedStatement ps = connection().prepareStatement(sql, returnMode);
            statements.add(ps);
            //statements.put(sql, ps);
        //} else {
        //    ps.clearParameters();
        //}

        batch = (mode == Mode.BATCH ? ps : null);

        return ps;
    }

    public void execute(String script) {
        if (script == null || script.length() == 0) return;

        String[] lines = script.split("@");

        String command = "";
        try (Statement sql = connection().createStatement()) {
            assert sql != null;

            for (String line : lines) {
                command = line.trim();
                if (command.charAt(0) == '#') continue; // ignore comment lines

                System.out.println(String.format("executing statement %s", command));
                sql.execute(command);
            }

            System.out.println("commiting...");
            connection().commit();
        } catch (SQLException e) {
            throw new PersistenceException(e, "Error executing SQL: %s", command);
        }
    }

    public long update(PreparedStatement statement)
        throws SQLException
    {
        if (mode == Mode.BATCH) {
            statement.addBatch();
        } else {
            statement.executeUpdate();

            if (commitMode == Mode.AUTO_COMMIT) {
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys != null && keys.next()) {
                        return keys.getLong(1);
                    }

                    return (keys != null && keys.next() == true ? keys.getLong(1) : 0);
                }
            }
        }

        return 0;
        //return (mode == Mode.AUTO_COMMIT ? statement.getGeneratedKeys() : null);
    }

    protected Connection connection()
            throws SQLException
    {
        if (connection == null) {
            connection = dataSource.getConnection();
            switch (mode) {
                case READ_ONLY:
                    connection.setReadOnly(true);
                    connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    connection.setAutoCommit(true);
                    break;
                case AUTO_COMMIT:
                    connection.setAutoCommit(true);
                    break;
                default:
                    connection.setAutoCommit(false);
            }
        }

        assert connection != null;

        return connection;
    }

    protected void closeStatements() {
        if (batch != null) {
            try { batch.executeBatch(); } catch (Exception e) {}
            batch = null;
        }

        if (statements == null) return;

        for (PreparedStatement ps : statements) {
        //for (PreparedStatement ps : statements.values()) {
            try { ps.close(); } catch (Exception e) {}
        }

        statements.clear();
    }

    protected void closeConnection()
    {
        if (connection != null) {
            if (commitMode != Mode.AUTO_COMMIT) {
                try { connection.commit(); }
                catch (SQLException e) {
                    throw new PersistenceException(e, "Error commiting JDBC connection");
                }
            }

            try { connection.close(); } catch (Exception e) {}

            connection = null;
        }
    }
}
