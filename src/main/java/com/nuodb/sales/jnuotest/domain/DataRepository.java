package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;
import com.nuodb.sales.jnuotest.dao.ConfigurationException;
import com.nuodb.sales.jnuotest.dao.PersistenceException;
import com.nuodb.sales.jnuotest.dao.SqlSession;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by nik on 7/5/15.
 */
public class DataRepository extends AbstractRepository<Data> {

    public DataRepository() {
        super("NuoTest.DATA", "groupId", "dataGuid", "instanceUID", "createdDateTime", "acquiredDateTime", "version", "active", "sizeOnDiskMB", "regionWeek");
    }

    @Override
    public void init()
        throws ConfigurationException
    {}

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

        //String sql = String.format(findBySql, getTableName(), "groupId", String.valueOf(data.getGroup()));
        long queryStart = System.currentTimeMillis();
        try (ResultSet existing = queryBy("groupId", data.getGroup())) {
            log.info(String.format("Uniqueness query complete; duration=%d ms", System.currentTimeMillis() - queryStart));
            long readStart = System.currentTimeMillis();
            while (existing.next()) {
                data = dataRows.get(existing.getString("instanceUID"));
                if (data != null) {
                    data.setActive(false);
                    total--;
                }
            }
            log.info(String.format("Uniqueness loop checked %d items; duration=%d ms", dataRows.size(), System.currentTimeMillis() - readStart));

            return total;
        } catch (SQLException e) {
            throw new PersistenceException(e, "Error DataRepository.checkUniqueness");
        }
    }

    @Override
    protected Data mapIn(ResultSet row) throws SQLException {
        Data data = new Data(row.getLong("id"), row.getString("dataGuid"));
        data.setGroup(row.getLong("groupId"));
        data.setInstanceUID(row.getString("instanceUID"));
        data.setCreatedDateTime(row.getDate("createdDateTime"));
        data.setAcquiredDateTime(row.getDate("acquiredDateTime"));
        data.setVersion(row.getInt("version"));
        data.setActive(row.getBoolean("active"));
        data.setSizeOnDiskMB(row.getFloat("sizeOnDiskMB"));
        data.setRegionWeek(row.getString("regionWeek"));

        return data;
    }

    @Override
    protected void mapOut(Data data, PreparedStatement update) throws SQLException {
        update.setLong(1, data.getGroup());
        update.setString(2, data.getDataGuid());
        update.setString(3, data.getInstanceUID());
        update.setDate(4, new java.sql.Date(data.getCreatedDateTime().getTime()));
        update.setDate(5, new java.sql.Date(data.getAcquiredDateTime().getTime()));
        update.setInt(6, data.getVersion());
        update.setBoolean(7, data.isActive());
        update.setFloat(8, data.getSizeOnDiskMB());
        update.setString(9, data.getRegionWeek());
    }
}
