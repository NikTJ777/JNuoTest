package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by nik on 7/5/15.
 */
public class DataRepository extends AbstractRepository<Data> {

    public DataRepository() {
        super("NuoTest.T_DATA", "groupId", "instanceUID", "name", "description", "path");
    }

    @Override
    protected Data mapIn(ResultSet row) throws SQLException {
        Data data = new Data(row.getLong("id"), row.getString("name"));
        data.setGroup(row.getLong("groupId"));
        data.setInstanceUID(row.getString("instanceUID"));
        data.setDescription(row.getString("description"));
        data.setPath(row.getString("path"));

        return data;
    }

    @Override
    protected void mapOut(Data data, PreparedStatement update) throws SQLException {
        update.setLong(1, data.getGroup());
        update.setString(2, data.getInstanceUID());
        update.setString(3, data.getName());
        update.setString(4, data.getDescription());
        update.setString(6, data.getPath());
    }
}
