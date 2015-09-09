package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;
import com.nuodb.sales.jnuotest.dao.ConfigurationException;
import com.nuodb.sales.jnuotest.dao.PersistenceException;
import com.nuodb.sales.jnuotest.dao.SqlSession;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by nik on 7/5/15.
 */
public class EventRepository extends AbstractRepository<Event> {

    private OwnerRepository ownerRepository;
    private GroupRepository groupRepository;
    private DataRepository dataRepository;

    public EventRepository(OwnerRepository ownerRepository, GroupRepository groupRepository, DataRepository dataRepository) {
        super("NuoTest.EVENT", "customerId", "ownerId", "eventGuid", "name", "description", "dateCreated", "lastUpdated", "region");

        this.ownerRepository = ownerRepository;
        this.groupRepository = groupRepository;
        this.dataRepository = dataRepository;
    }

    @Override
    public void init()
        throws ConfigurationException
    {
        if (ownerRepository == null || groupRepository == null || dataRepository == null)
            throw new ConfigurationException("Dependencies have not been set: ownerRepository %s; groupRepository %s; dataRepository %s.",
                    ownerRepository, groupRepository, dataRepository);
    }

    /**
     * Retrieve the details of an Event and all its associated objects.
     *
     * @param eventId
     * @return an EventDetails object that has references to associated Owner, Groups, and Data
     *
     * @throws PersistenceException if there is any error in retrieving the data.
     */
    public EventDetails getDetails(long eventId)
        throws PersistenceException
    {
        EventDetails result = null;
        try {

            Event event = findById(eventId);
            if (event == null) {
                log.info(String.format("Event %d not found.", eventId));
                return null;
            }

            Owner owner = ownerRepository.findById(event.getOwnerId());
            result = new EventDetails(event, owner);

            List<Group> groups = groupRepository.findAllBy("eventId", eventId);
            result.setGroups(groups);

            log.info(String.format("retrieved %d groups", groups.size()));

            Long[] groupIds = new Long[groups.size()];
            for (int gx = 0; gx < groupIds.length; gx++) {
                groupIds[gx] = groups.get(gx).getId();
            }

            List<Data> data = dataRepository.findAllBy("groupId", groupIds);
            result.setData(data);

            log.info(String.format("retrieved %d data records", data.size()));

        } catch (Exception e) {
            e.printStackTrace(System.out);
            log.info(String.format("getDetails exception: %s", e.toString()));
            throw new PersistenceException(e, "Error retrieving EventView %d", eventId);
        }

        return result;
    }


    @Override
    protected Event mapIn(ResultSet row) throws SQLException {
        Event event = new Event(row.getLong("id"), row.getLong("customerId"), row.getString("eventGuid"));
        event.setOwnerId(row.getLong("ownerId"));
        event.setName(row.getString("name"));
        event.setDescription(row.getString("description"));
        event.setDateCreated(row.getDate("dateCreated"));
        event.setLastUpdated(row.getDate("lastUpdated"));
        event.setRegion(row.getString("region"));

        return event;
    }

    @Override
    protected void mapOut(Event event, PreparedStatement update) throws SQLException {
        update.setLong(1, event.getCustomerId());
        update.setLong(2, event.getOwnerId());
        update.setString(3, event.getEventGuid());
        update.setString(4, event.getName());
        update.setString(5, event.getDescription());
        update.setDate(6, new java.sql.Date(event.getDateCreated().getTime()));
        update.setDate(7, new java.sql.Date(event.getLastUpdated().getTime()));
        update.setString(8, event.getRegion());
    }
}
