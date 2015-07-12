package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.Entity;

/**
 * Created by nik on 7/2/15.
 */
public class Owner extends Entity {

    private String name;
    private long masterAliasId;

    protected Owner(long id, String name) {
        super(id);
        this.name = name;
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

    public void setMasterAlias(long masterAlias) {
        this.masterAliasId = masterAlias;
    }

    public long getMasterAlias() {
        return masterAliasId;
    }

}
