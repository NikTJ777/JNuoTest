package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

import java.util.Date;

/**
 * Created by nik on 7/2/15.
 */
public class Event extends Entity {
    private long id;

    private long ownerId;

    private String name;
    private String description;

    private Date date;

    protected Event(long id, String name) {
        super(id);
    }

    public Event() {
        super();
    }

    public long getId() {
        return id;
    }

    public long getOwner() {
        return ownerId;
    }

    public void setOwner(long owner) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
