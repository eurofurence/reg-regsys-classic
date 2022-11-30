package org.eurofurence.regsys.web.services;

public class NotFound extends AbstractAttendeeListService {
    @Override
    protected void authenticate() {
    }

    @Override
    protected AbstractResponseDTO createResponse() {
        throw new ServiceException("notfound", getRequestId());
    }
}
