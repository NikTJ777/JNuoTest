package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

import java.util.Date;

/**
 * Created by nik on 7/2/15.
 */
public class Event extends Entity {

    private long customerId;
    private long ownerId;
    private String eventGuid;
    private String name;
    private String description;
    private Date dateCreated;
    private Date lastUpdated;
    private String region;

    public Event(long id, long customerId, String eventGuid) {
        super(id);
        this.customerId = customerId;
        this.eventGuid = eventGuid;
    }

    public Event() {
        super();
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getEventGuid() {
        return eventGuid;
    }

    public void setEventGuid(String eventGuid) {
        this.eventGuid = eventGuid;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long owner) {
        this.ownerId = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date date) {
        this.dateCreated = date;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
