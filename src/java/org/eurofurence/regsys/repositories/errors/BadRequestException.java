package org.eurofurence.regsys.repositories.errors;

public class BadRequestException extends DownstreamWebErrorException {
    public BadRequestException(ErrorDto response) {
        super(response);
    }

    public BadRequestException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
