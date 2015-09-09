package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

import java.util.Date;

/**
 * Created by nik on 7/2/15.
 */
public class Data extends Entity {

    private long groupId;
    private String dataGuid;
    private String instanceUID;
    private Date createdDateTime;
    private Date acquiredDateTime;
    private int version;
    private boolean active;
    private float sizeOnDiskMB;
    private String regionWeek;

    protected Data(long id, String dataGuid) {
        super(id);
        this.dataGuid = dataGuid;
    }

    public Data() {
        super();
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getDataGuid() {
        return dataGuid;
    }

    public void setDataGuid(String dataGuid) {
        this.dataGuid = dataGuid;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getAcquiredDateTime() {
        return acquiredDateTime;
    }

    public void setAcquiredDateTime(Date acquiredDateTime) {
        this.acquiredDateTime = acquiredDateTime;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public float getSizeOnDiskMB() {
        return sizeOnDiskMB;
    }

    public void setSizeOnDiskMB(float sizeOnDiskMB) {
        this.sizeOnDiskMB = sizeOnDiskMB;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getRegionWeek() {
        return regionWeek;
    }

    public void setRegionWeek(String regionWeek) {
        this.regionWeek = regionWeek;
    }
}
