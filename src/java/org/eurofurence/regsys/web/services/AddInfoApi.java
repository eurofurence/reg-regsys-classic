package org.eurofurence.regsys.web.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eurofurence.regsys.repositories.attendees.AddInfo;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.web.servlets.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An API to get and set an additional info flag value.
 */
public class AddInfoApi extends Service {
    // --- parameter constants ---

    private static final String PARAM_TOKEN   = "token";
    private static final String PARAM_ID      = "id";
    private static final String PARAM_AREA    = "area";

    // --- variables ---

    private long attendeeId = -1L;
    private String area = "";
    private Set<String> allowedAreas = new HashSet<>();

    private AddInfo addInfo = null;
    private final AttendeeService attendeeService = new AttendeeService();

    // --- auth ---

    protected Map<String,String> authForArea() {
        Map<String,String> allowed = new HashMap<>();

        Configuration.DownstreamConfig downstream = getConfiguration().downstream;
        if (downstream != null) {
            if (downstream.ddToken != null && !"".equals(downstream.ddToken)) {
                allowed.put("dealerreg", downstream.ddToken);
            }
        }
        return allowed;
    }

    protected void authenticate() {
        String token = getRequest().getParameter(PARAM_TOKEN);
        if (token == null || "".equals(token))
            throw new ServiceException("security.invalid.token", getRequestId());

        allowedAreas = authForArea().entrySet().stream()
                .filter(e -> e.getValue().equals(token))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (allowedAreas.isEmpty())
            throw new ServiceException("security.invalid.token", getRequestId());
    }

    // --- parameters and preparations ---

    protected void parseParameters() {
        String idStr = getRequest().getParameter(PARAM_ID);
        if ((idStr == null) || !idStr.matches("[1-9][0-9]*"))
            throw new ServiceException("addinfoapi.validation.failure.id", getRequestId());
        attendeeId = Long.parseLong(idStr);

        String areaName = getRequest().getParameter(PARAM_AREA);
        if (areaName == null || "".equals(areaName))
            throw new ServiceException("addinfoapi.validation.failure.area", getRequestId());

        // check that the package is allowed to be manipulated by this api and token
        if (!allowedAreas.contains(areaName))
            throw new ServiceException("security.access.denied", getRequestId());

        area = areaName;
    }

    protected static class ResponseDTO {
        @JsonProperty("ok")
        public boolean ok = true;

        @JsonProperty("enabled")
        public boolean enabled;
    }

    protected void process() {
        try {
            RequestAuth downstreamAuth = new RequestAuth();
            downstreamAuth.apiToken = getConfiguration().downstream.apiToken;
            String requestId = getRequestId();
            attendeeService.performGetAttendee(attendeeId, downstreamAuth, requestId); // ensure present etc.

            try {
                addInfo = attendeeService.performGetAdditionalInfo(attendeeId, area, downstreamAuth, requestId);
            } catch (NotFoundException ignore) {
                addInfo = null;
            }

            if (getMethod() == HttpMethod.POST) {
                addInfo = new AddInfo();
                addInfo.put("enabled", "yes");
                attendeeService.performSetAdditionalInfo(attendeeId, area, addInfo, downstreamAuth, requestId);
            } else if (getMethod() == HttpMethod.DELETE) {
                try {
                    attendeeService.performDeleteAdditionalInfo(attendeeId, area, downstreamAuth, requestId);
                } catch (NotFoundException ignore) {}
                addInfo = null;
            }
        } catch (NotFoundException e) {
            throw new ServiceException("addinfoapi.attendee.notfound", getRequestId());
        } catch (DownstreamException e) {
            throw new ServiceException("addinfoapi.downstream.failure", getRequestId());
        }
    }

    protected ResponseDTO createResponse() {
        ResponseDTO response = new ResponseDTO();
        response.enabled = addInfo != null;
        return response;
    }

    @Override
    public void handle() {
        try {
            authenticate();
            parseParameters();
            process();
            jsonServiceResponse(createResponse());
        } catch (ServiceException e) {
            jsonServiceResponse(e);
        }
    }
}
