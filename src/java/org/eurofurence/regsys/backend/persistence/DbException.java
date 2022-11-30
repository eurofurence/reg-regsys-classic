package org.eurofurence.regsys.backend.persistence;

public class DbException extends Exception {
    public DbException(String msg) {
        super(msg);
    }

    public DbException(String msg, Throwable arg1) {
        super(msg, arg1);
    }
}
