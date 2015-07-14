package com.nuodb.sales.jnuotest.dao;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by nik on 7/5/15.
 */
public abstract class AbstractRepository<T extends Entity> implements Repository<T> {

    private final String tableName;
    private final String[] columns;
    private final Map<String, Integer> ordinal;

    private final String fields;
    private final String params;

    public static enum UpdateMode { CREATE, UPDATE };

    protected static Logger log = Logger.getLogger("Repository");


    /**
     * query string for findById
     * @param String table name
     * @param long id
     */
    protected static String findSql = "SELECT * from %s where id = '%d';'";

    /**
     * query string for findALlBy
     * @param String tableName
     * @param String columnName
     */
    protected static String findBySql = "SELECT * from %s where %s = '%s'";

    /**
     * query string for update statement
     * @param String comma-separated list of columns to update ex: name, date
     * @param String table name
     * @param String comma-separated list of parameter names ex: ?name, ?date
     */
    protected static String persistSql = "INSERT into %s (%s) values (%s);";

    /**
     * sql statement to update an existing row
     * @param String comma-separated list of columns to update; ex: name, date
     * @param String table name
     * @param String comma-separated list of parameter names; ex: ?name, ?date
     * @param long record id
     */
    protected static String updateSql = "UPDATE %s SET %s = (%s) where id = '%d';";

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

        ordinal = new HashMap<String, Integer>(columns.length * 2);
        for (int cx = 0; cx < columns.length; cx++) {
            ordinal.put(columns[cx], new Integer(cx+2));    // skip id, and ordinal is 1-based
        }
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public T findById(long id) {

        String sql = String.format(findSql, tableName, id);
        try (ResultSet row = SqlSession.getCurrent().getStatement(sql).executeQuery()) {
            if (row == null) return null;

            return mapIn(row);
        } catch (SQLException e) {
            log.info(String.format("FindById failed due to %s", e.toString()));
            return null;
        }
    }

    @Override
    public long persist(T entity)
            throws PersistenceException
    {

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

    @Override
    public void update(long id, String columns, Object ... values)
            throws PersistenceException
    {

        StringBuilder builder = new StringBuilder();
        for (int x = values.length; x > 0; x--) {
            if (builder.length() > 0) builder.append(", ");
            builder.append('?'); //.append(name);
        }

        String params = builder.toString();

        String sql = String.format(updateSql, tableName, columns, params, id);
        try {
            SqlSession session = SqlSession.getCurrent();
            PreparedStatement update = session.getStatement(sql);
            setParams(update, columns, values);
        } catch (SQLException e) {
            throw new PersistenceException(e, "Error updating table %s, id %d", getTableName(), id);
        }

    }

    protected abstract T mapIn(ResultSet row) throws SQLException;

    protected abstract void mapOut(T entity, PreparedStatement sql) throws SQLException;

    protected void setParams(PreparedStatement sp, String columns, Object[] values)
        throws PersistenceException, SQLException
    {
        String[] fields = columns.split(", ");
        if (fields.length < values.length)
            throw new PersistenceException("Invalid update request: insufficient field names for values: %s, %a", columns, values);

        for (int vx = 0; vx < values.length; vx++) {
            Class type = values[vx].getClass();


            if (type == Integer.class) {
                sp.setInt(vx+1, (Integer) values[vx]);
            }
        }

    }
}
