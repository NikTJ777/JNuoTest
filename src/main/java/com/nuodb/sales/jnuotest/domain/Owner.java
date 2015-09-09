package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

import java.util.Date;

public class Owner extends Entity
{
    private long customerId;
    private String ownerGuid;
    private Date dateCreated;
    private Date lastUpdated;
    private String name;
    private long masterAliasId;

    private String region;

    protected Owner(long id, long customerId, String ownerGuid) {
        super(id);
        this.customerId = customerId;
        this.ownerGuid = ownerGuid;
    }

    public Owner() {
        super();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMasterAliasId(long masterAlias) {
        this.masterAliasId = masterAlias;
    }

    public long getMasterAliasId() {
        return masterAliasId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getOwnerGuid() {
        return ownerGuid;
    }

    public void setOwnerGuid(String ownerGuid) {
        this.ownerGuid = ownerGuid;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
