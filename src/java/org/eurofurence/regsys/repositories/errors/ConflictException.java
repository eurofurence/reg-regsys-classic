package org.eurofurence.regsys.repositories.errors;

public class ConflictException extends DownstreamWebErrorException {
    public ConflictException(ErrorDto response) {
        super(response);
    }

    public ConflictException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
