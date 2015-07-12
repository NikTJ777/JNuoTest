package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

import java.util.Date;

/**
 * Created by nik on 7/2/15.
 */
public class Group extends Entity {

    private long eventId;

    private String name;
    private String description;

    private int dataCount;

    private Date date;

    protected Group(long id, String name) {
        super(id);

        this.name = name;
    }

    public Group() {
        super();
    }

    public long getEvent() {
        return eventId;
    }

    public void setEvent(long event) {
        this.eventId = event;
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

    public int getDataCount() {
        return dataCount;
    }

    public void setDataCount(int dataCount) {
        this.dataCount = dataCount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
