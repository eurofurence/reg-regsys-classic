package org.eurofurence.regsys.repositories.errors;

public class InternalServerErrorException extends DownstreamWebErrorException {
    public InternalServerErrorException(ErrorDto response) {
        super(response);
    }

    public InternalServerErrorException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
