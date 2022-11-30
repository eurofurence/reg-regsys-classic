package org.eurofurence.regsys.backend;

public class PermissionDeniedException extends Exception {
    private static final long serialVersionUID = -6436450072641006357L;

    public PermissionDeniedException(String msg) {
        super(msg);
    }

}

