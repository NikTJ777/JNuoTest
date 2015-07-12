package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by nik on 7/5/15.
 */
public class EventRepository extends AbstractRepository<Event> {

    public EventRepository() {
        super("NuoTest.T_EVENT", "ownerId", "name", "description", "date");
    }

    @Override
    protected Event mapIn(ResultSet row) throws SQLException {
        Event event = new Event(row.getLong("id"), row.getString("name"));
        event.setOwner(row.getLong("ownerId"));
        event.setDescription(row.getString("description"));
        event.setDate(row.getDate("date"));

        return event;
    }

    @Override
    protected void mapOut(Event event, PreparedStatement update) throws SQLException {
        update.setLong(1, event.getOwner());
        update.setString(2, event.getName());
        update.setString(3, event.getDescription());
        update.setDate(4, new java.sql.Date(event.getDate().getTime()));
    }
}
