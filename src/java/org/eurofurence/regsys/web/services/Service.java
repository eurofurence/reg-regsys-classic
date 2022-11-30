package org.eurofurence.regsys.web.services;

import org.eurofurence.regsys.web.servlets.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *  Semi-abstract base class that represents a service endpoint in a web application.
 *
 *  Subclass to implement an actual endpoint.
 */
public abstract class Service extends RequestHandler {
    /**
     * nice default implementation for json/utf-8/post style services.
     *
     * You can call it in your version of handleRequest
     */
    protected void jsonServiceResponse(Object dto) {
        getResponse().setContentType("application/json;charset=UTF-8");
        if (dto instanceof ServiceException)
            getResponse().setStatus(((ServiceException) dto).getHttpStatus());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(getResponse().getOutputStream(), dto);
        } catch (Exception e) {
            throw new ServiceException("json.mapping.failure", getRequestId(), e);
        }
    }
}
