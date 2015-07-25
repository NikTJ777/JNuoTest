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
        super("NuoTest.T_OWNER", "name", "masterAliasId", "region");
    }

    @Override
    public void init()
        throws ConfigurationException
    {}

    @Override
    public Owner mapIn(ResultSet row) throws SQLException {
        Owner owner = new Owner(row.getLong("id"), row.getString("name"));
        owner.setMasterAlias(row.getLong("masterAliasId"));
        owner.setRegion(row.getString("region"));

        return owner;
    }

    @Override
    public void mapOut(Owner owner, PreparedStatement update) throws SQLException {
        update.setString(1, owner.getName());
        update.setLong(2, owner.getMasterAlias());
        update.setString(3, owner.getRegion());
    }
}
