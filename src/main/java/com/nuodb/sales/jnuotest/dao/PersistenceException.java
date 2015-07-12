package com.nuodb.sales.jnuotest.dao;

/**
 * Created by nik on 7/5/15.
 */
public class PersistenceException extends RuntimeException {

    public PersistenceException(String msg, Object ... params) {
        super(String.format(msg, params));
    }

    public PersistenceException(Exception cause, String msg, Object... params) {
        super(String.format(msg, params), cause);
    }
}
