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
        super("NuoTest.T_GROUP", "eventId", "name", "description", "dataCount", "date");
    }

    @Override
    public void init()
        throws ConfigurationException
    {}

    @Override
    protected Group mapIn(ResultSet row) throws SQLException {
        Group group = new Group(row.getLong("id"), row.getString("name"));
        group.setEvent(row.getLong("eventId"));
        group.setDescription(row.getString("description"));
        group.setDataCount(row.getInt("dataCount"));
        group.setDate(row.getDate("date"));

        return group;
    }

    @Override
    protected void mapOut(Group group, PreparedStatement update) throws SQLException {
        update.setLong(1, group.getEvent());
        update.setString(2, group.getName());
        update.setString(3, group.getDescription());
        update.setInt(4, group.getDataCount());
        update.setDate(5, new java.sql.Date(group.getDate().getTime()));
    }
}
