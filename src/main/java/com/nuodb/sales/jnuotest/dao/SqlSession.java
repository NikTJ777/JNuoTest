package com.nuodb.sales.jnuotest.dao;


import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nik on 7/5/15.
 */
public class SqlSession implements AutoCloseable {

    private Mode mode;
    private Connection connection;
    private PreparedStatement  batch;
    private Map<String, PreparedStatement> statements;

    private static DataSource dataSource;
    private static ThreadLocal<SqlSession> current = new ThreadLocal<SqlSession>();

    private SqlSession(Mode mode) {
        this.mode = mode;
    }

    public enum Mode { AUTO_COMMIT, TRANSACTIONAL, BATCH };

    public static void init(DataSource ds) {
        dataSource = ds;
    }

    public static SqlSession start(Mode mode) {
        SqlSession session = current.get();

        if (session != null) {
            session.close();
        }

        session = new SqlSession(mode);
        current.set(session);

        return session;
    }

    public static SqlSession getCurrent() {
        SqlSession session = current.get();
        return (session != null ? session : start(Mode.AUTO_COMMIT));
    }

    public void rollback() {
        if (connection != null && mode != Mode.AUTO_COMMIT) {
            try { connection.rollback(); }
            catch (SQLException e) {}
        }
    }

    @Override
    public void close() {
        closeStatements();
        closeConnection();
        current.set(null);
    }

    public PreparedStatement getStatement(String sql) throws SQLException {
        if (statements == null) {
            statements = new HashMap<String, PreparedStatement>(16);
        }

        PreparedStatement ps = statements.get(sql);

        if (ps == null) {
            int returnMode = (mode == Mode.AUTO_COMMIT ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
            //int returnMode = Statement.RETURN_GENERATED_KEYS;
            ps = connection().prepareStatement(sql, returnMode);
            statements.put(sql, ps);
        } else {
            ps.clearParameters();
        }

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
                System.out.println(String.format("executing statement %s", command));
                sql.execute(command);
            }

            System.out.println("commiting...");
            connection().commit();
        } catch (SQLException e) {
            throw new PersistenceException(e, "Error executing SQL: %s", command);
        }
    }

    public ResultSet update(PreparedStatement statement)
        throws SQLException
    {
        if (mode == Mode.BATCH) {
            statement.addBatch();
        } else {
            statement.executeUpdate();
        }

        return (mode == Mode.AUTO_COMMIT ? statement.getGeneratedKeys() : null);
    }

    protected Connection connection()
            throws SQLException
    {
        if (connection == null) {
            connection = dataSource.getConnection();
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

        for (PreparedStatement ps : statements.values()) {
            try { ps.close(); } catch (Exception e) {}
        }

        statements.clear();
    }

    protected void closeConnection()
    {
        if (connection != null) {
            if (mode != Mode.AUTO_COMMIT) {
                try { connection.commit(); }
                catch (SQLException e) {
                    throw new PersistenceException(e, "Error commiting JDBC connection");
                }
            }

            try { connection.close(); } catch (Exception e) {}
        }
    }
}
