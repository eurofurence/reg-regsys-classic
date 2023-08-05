package org.eurofurence.regsys.web.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.eurofurence.regsys.backend.Logging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value = {"cause", "stackTrace", "localizedMessage", "suppressed"}, ignoreUnknown = true)
public class ServiceException extends RuntimeException {
    private static final long serialVersionUID = 4950034475261838535L;

    private static final int DEFAULT_HTTP_STATUS = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    private int httpStatus;
    private Date timestamp;
    private List<String> details = new ArrayList<>();
    private String requestId;

    /**
     * Construct a sensible response in case of an error.
     *
     * @param message textual message or error code in dotted notation (in case you externalize strings)
     * @param httpStatus use the constants from HttpServletResponse
     * @param cause inner exception
     */
    public ServiceException(String message, int httpStatus, String requestId, Exception cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        timestamp = new Date();
        this.requestId = requestId;
        log();
    }

    public ServiceException(String message, String requestId, Exception cause) {
        this(message, DEFAULT_HTTP_STATUS, requestId, cause);
    }

    public ServiceException(String message, int httpStatus, String requestId) {
        this(message, httpStatus, requestId, null);
    }

    public ServiceException(String message, String requestId) {
        this(message, DEFAULT_HTTP_STATUS, requestId, null);
    }

    @JsonIgnore
    public int getHttpStatus() {
        return httpStatus;
    }

    @JsonProperty("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    @JsonProperty("ok")
    public boolean isOk() {
        return false;
    }

    @Override
    @JsonProperty("message")
    public String getMessage() {
        return super.getMessage();
    }

    @JsonProperty("details")
    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }

    private void log() {
        try {
            Logging.error("[" + requestId + "] service call failed: " + getMessage());
            if (getCause() != null) {
                Logging.exception(getCause());
            }
        } catch (Exception ignored) {
        }
    }
}
