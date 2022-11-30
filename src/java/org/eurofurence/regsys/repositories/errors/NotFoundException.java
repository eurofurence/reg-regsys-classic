package org.eurofurence.regsys.repositories.errors;

public class NotFoundException extends DownstreamWebErrorException {
    public NotFoundException(ErrorDto response) {
        super(response);
    }

    public NotFoundException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
