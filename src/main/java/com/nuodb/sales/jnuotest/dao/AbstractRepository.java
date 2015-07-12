package com.nuodb.sales.jnuotest.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by nik on 7/5/15.
 */
public abstract class AbstractRepository<T extends Entity> implements Repository<T> {

    private final String tableName;
    private final String[] columns;

    private final String fields;
    private final String params;

    public static enum UpdateMode { CREATE, UPDATE };

    /**
     * query string for query
     * @param String table name
     * @param long id
     */
    private static String findSql = "SELECT * from %s where id = '%d';'";

    /**
     * query string for update statement
     * @param String comma-separated list of columns to update ex: name, date
     * @param String table name
     * @param String comma-separated list of parameter names ex: ?name, ?date
     */
    private static String persistSql = "INSERT into %s (%s) values (%s);";

    /**
     * sql statement to update an existing row
     * @param String comma-separated list of columns to update; ex: name, date
     * @param String table name
     * @param String comma-separated list of parameter names; ex: ?name, ?date
     * @param long record id
     */
    private static String updateSql = "UPDATE into %s (%s) values (%s) where id = '%d';";

    public AbstractRepository(String tableName, String... columns) {
        this.tableName = tableName;
        this.columns = columns;

        fields = Arrays.toString(columns).replaceAll("\\[|\\]", "");

        StringBuilder builder = new StringBuilder();
        for (String name : columns) {
            if (builder.length() > 0) builder.append(", ");
            builder.append('?'); //.append(name);
        }

        params = builder.toString();
    }

    @Override
    public T findById(long id) {

        String sql = String.format(findSql, tableName, id);
        try (ResultSet row = SqlSession.getCurrent().getStatement(sql).executeQuery()) {
            if (row == null) return null;

            return mapIn(row);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public long persist(T entity) throws PersistenceException {

        if (entity.isPersistent()) {
            throw new PersistenceException("Attempt to persist already persistent object %s", entity.toString());
        }

        String sql = String.format(persistSql, tableName, fields, params);
        try {
            SqlSession session = SqlSession.getCurrent();
            PreparedStatement update = session.getStatement(sql);
            mapOut(entity, update);

            ResultSet keys = session.update(update);

            if (keys != null && keys.next()) {
                return keys.getLong(1);
            }

            return 0;

        } catch (SQLException e) {
            throw new PersistenceException(e, "Error persisting new Entity %s", entity.toString());
        }
    }

    protected abstract T mapIn(ResultSet row) throws SQLException;

    protected abstract void mapOut(T entity, PreparedStatement sql) throws SQLException;

}
