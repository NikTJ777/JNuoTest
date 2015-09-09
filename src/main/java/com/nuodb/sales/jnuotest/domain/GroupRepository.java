package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;
import com.nuodb.sales.jnuotest.dao.ConfigurationException;
import com.nuodb.sales.jnuotest.dao.PersistenceException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by nik on 7/5/15.
 */
public class GroupRepository extends AbstractRepository<Group> {

    public GroupRepository() {
        super("NuoTest.\"GROUP\"", "eventId", "groupGuid", "description", "dataCount", "dateCreated", "lastUpdated", "region", "week");
    }

    @Override
    public void init()
        throws ConfigurationException
    {}

    @Override
    protected Group mapIn(ResultSet row) throws SQLException {
        Group group = new Group(row.getLong("id"), row.getString("groupGuid"));
        group.setEvent(row.getLong("eventId"));
        group.setDescription(row.getString("description"));
        group.setDataCount(row.getInt("dataCount"));
        group.setDateCreated(row.getDate("dateCreated"));
        group.setLastUpdated(row.getDate("lastUpdated"));
        group.setRegion(row.getString("region"));
        group.setWeek(row.getLong("week"));

        return group;
    }

    @Override
    protected void mapOut(Group group, PreparedStatement update) throws SQLException {
        update.setLong(1, group.getEvent());
        update.setString(2, group.getGroupGuid());
        update.setString(3, group.getDescription());
        update.setInt(4, group.getDataCount());
        update.setDate(5, new java.sql.Date(group.getDateCreated().getTime()));
        update.setDate(6, new java.sql.Date(group.getLastUpdated().getTime()));
        update.setString(7, group.getRegion());
        update.setLong(8, group.getWeek());
    }
}
