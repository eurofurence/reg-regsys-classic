package org.eurofurence.regsys.repositories.errors;

public class ForbiddenException extends DownstreamWebErrorException {
    public ForbiddenException(ErrorDto response) {
        super(response);
    }

    public ForbiddenException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
