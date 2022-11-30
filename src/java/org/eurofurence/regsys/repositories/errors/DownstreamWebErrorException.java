package org.eurofurence.regsys.repositories.errors;

public class DownstreamWebErrorException extends DownstreamException {
    private final ErrorDto err;

    public DownstreamWebErrorException(ErrorDto response) {
        super(response.message);
        err = response;
    }

    public DownstreamWebErrorException(ErrorDto response, Throwable cause) {
        super(response.message, cause);
        err = response;
    }

    public ErrorDto getErr() {
        return err;
    }
}
