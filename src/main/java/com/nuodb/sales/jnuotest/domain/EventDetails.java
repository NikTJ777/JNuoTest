package com.nuodb.sales.jnuotest.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nik on 7/15/15.
 */
public class EventDetails {

    private Event event;
    private Owner owner;
    private List<Group> groups;
    private List<Data> data;

    public EventDetails(Event event, Owner owner) {
        this.event = event;
        this.owner = owner;
    }

    public String getName() {
        return event.getName();
    }

    public String getDescription() {
        return event.getDescription();
    }

    public Date getDate() {
        return event.getDate();
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }
}
