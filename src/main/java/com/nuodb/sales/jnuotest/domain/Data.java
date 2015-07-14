package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

/**
 * Created by nik on 7/2/15.
 */
public class Data extends Entity {

    private long groupId;
    private String instanceUID;
    private String name;
    private String description;
    private String path;
    private boolean active;

    protected Data(long id, String name) {
        super(id);
        this.name = name;
    }

    public Data() {
        super();
    }

    public long getGroup() {
        return groupId;
    }

    public void setGroup(long group) {
        this.groupId = group;
    }

    public String getInstanceUID() {
        return instanceUID;
    }

    public void setInstanceUID(String instanceUID) {
        this.instanceUID = instanceUID;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
