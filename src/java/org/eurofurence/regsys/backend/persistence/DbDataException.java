package org.eurofurence.regsys.backend.persistence;

public class DbDataException extends DbException {
    public DbDataException(String msg) {
        super(msg);
    }

    public DbDataException(String msg, Throwable arg1) {
        super(msg, arg1);
    }
}
