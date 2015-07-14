package com.nuodb.sales.jnuotest.dao;

/**
 * Created by nik on 7/2/15.
 */
public interface Repository<T extends Entity> {
    public T findById(long id);

    public long persist(T entity);

    public void update(long id, String columns, Object ... values);
}
