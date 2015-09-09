package com.nuodb.sales.jnuotest.domain;

import com.nuodb.sales.jnuotest.dao.AbstractRepository;
import com.nuodb.sales.jnuotest.dao.ConfigurationException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by nik on 7/5/15.
 */
public class OwnerRepository extends AbstractRepository<Owner> {

    public OwnerRepository() {
        super("NuoTest.OWNER", "customerId", "ownerGuid", "dateCreated", "lastUpdated", "name", "masterAliasId", "region");
    }

    @Override
    public void init()
        throws ConfigurationException
    {}

    @Override
    public Owner mapIn(ResultSet row) throws SQLException {
        Owner owner = new Owner(row.getLong("id"), row.getLong("customerId"), row.getString("ownerGuid"));
        owner.setDateCreated(row.getDate("dateCreated"));
        owner.setLastUpdated(row.getDate("lastUpdated"));
        owner.setName(row.getString("name"));
        owner.setMasterAliasId(row.getLong("masterAliasId"));
        owner.setRegion(row.getString("region"));

        return owner;
    }

    @Override
    public void mapOut(Owner owner, PreparedStatement update) throws SQLException {
        update.setLong(1, owner.getCustomerId());
        update.setString(2, owner.getOwnerGuid());
        update.setDate(3, new java.sql.Date(owner.getDateCreated().getTime()));
        update.setDate(4, new java.sql.Date(owner.getLastUpdated().getTime()));
        update.setString(5, owner.getName());
        update.setLong(6, owner.getMasterAliasId());
        update.setString(7, owner.getRegion());
    }
}
