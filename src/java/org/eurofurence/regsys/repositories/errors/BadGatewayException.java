package org.eurofurence.regsys.repositories.errors;

public class BadGatewayException extends DownstreamWebErrorException {
    public BadGatewayException(ErrorDto response) {
        super(response);
    }

    public BadGatewayException(ErrorDto response, Throwable cause) {
        super(response, cause);
    }
}
