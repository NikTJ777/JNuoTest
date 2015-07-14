package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;
import com.nuodb.sales.jnuotest.dao.PersistenceException;
import com.nuodb.sales.jnuotest.dao.SqlSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by nik on 7/5/15.
 */
public class DataRepository extends AbstractRepository<Data> {

    public DataRepository() {
        super("NuoTest.T_DATA", "groupId", "instanceUID", "name", "description", "path", "active");
    }

    /**
     * Check the uniqueness of a set of data rows.
     * Intended to be called prior to committing a set of new Data rows.
     *
     * This method marks any (and all) duplicate Data objects as inactive (leaving the original as active),
     * and returns the total number of unique rows.
     *
     * @param dataRows Map&lt;String, Data&gt;
     *
     * @return the total number of unique rows
     *
     * @throws PersistenceException
     */
    public int checkUniqueness(Map<String, Data> dataRows)
        throws PersistenceException
    {
        Data data = dataRows.values().iterator().next();
        if (data == null) return 0;

        int total = dataRows.size();

        String sql = String.format(findBySql, getTableName(), "groupId", String.valueOf(data.getGroup()));
        try (ResultSet existing = SqlSession.getCurrent().getStatement(sql).executeQuery()) {
            while (existing.next()) {
                data = dataRows.get(existing.getString("instanceUID"));
                if (data != null) {
                    data.setActive(false);
                    total--;
                }
            }

            return total;
        } catch (SQLException e) {
            throw new PersistenceException(e, "Error DataRepository.checkUniqueness");
        }
    }

    @Override
    protected Data mapIn(ResultSet row) throws SQLException {
        Data data = new Data(row.getLong("id"), row.getString("name"));
        data.setGroup(row.getLong("groupId"));
        data.setInstanceUID(row.getString("instanceUID"));
        data.setDescription(row.getString("description"));
        data.setPath(row.getString("path"));
        data.setActive(row.getBoolean("active"));

        return data;
    }

    @Override
    protected void mapOut(Data data, PreparedStatement update) throws SQLException {
        update.setLong(1, data.getGroup());
        update.setString(2, data.getInstanceUID());
        update.setString(3, data.getName());
        update.setString(4, data.getDescription());
        update.setString(5, data.getPath());
        update.setBoolean(6, data.isActive());
    }
}
