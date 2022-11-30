package org.eurofurence.regsys.repositories.errors;

public class BodySerializationException extends DownstreamException {
    public BodySerializationException(String message) {
        super(message);
    }

    public BodySerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
