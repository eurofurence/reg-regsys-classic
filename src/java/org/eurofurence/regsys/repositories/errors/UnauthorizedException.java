package org.eurofurence.regsys.repositories.errors;

public class UnauthorizedException extends DownstreamWebErrorException {
    public UnauthorizedException(ErrorDto response) {
        super(response);
    }

    public UnauthorizedException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
