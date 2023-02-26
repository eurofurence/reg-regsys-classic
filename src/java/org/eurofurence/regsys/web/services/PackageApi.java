package org.eurofurence.regsys.web.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.web.servlets.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * An API to get the package state (GET), or add (POST) or remove (DELETE) a package.
 * All three parameters must always be set as query parameters.
 */
public class PackageApi extends Service {
    // --- parameter constants ---

    private static final String PARAM_TOKEN   = "token";
    private static final String PARAM_ID      = "id";
    private static final String PARAM_PACKAGE = "package";

    // --- variables ---

    private long attendeeId = -1L;
    private String allowedPackageName = "";

    private Attendee attendee = new Attendee();
    private AttendeeService attendeeService = new AttendeeService();

    // --- auth ---

    protected Map<String,String> authForPackageByToken() {
        Map<String,String> allowed = new HashMap<>();

        Configuration.DownstreamConfig downstream = getConfiguration().downstream;
        if (downstream != null) {
            // TODO: hardcoded for now
            if (downstream.ddToken != null && !"".equals(downstream.ddToken)) {
                allowed.put(downstream.ddToken, "dealer-table");
            }
            if (downstream.boatToken != null && !"".equals(downstream.boatToken)) {
                allowed.put(downstream.boatToken, "boat-trip");
            }
        }
        return allowed;
    }

    protected void authenticate() {
        String token = getRequest().getParameter(PARAM_TOKEN);
        if (token == null)
            throw new ServiceException("security.invalid.token", getRequestId());

        Map<String, String> allowed = authForPackageByToken();
        if (allowed.containsKey(token)) {
            allowedPackageName = allowed.get(token);
            return;
        }
        throw new ServiceException("security.invalid.token", getRequestId());
    }

    // --- parameters and preparations ---

    protected void parseParameters() {
        String idStr = getRequest().getParameter(PARAM_ID);
        if ((idStr == null) || !idStr.matches("[1-9][0-9]*"))
            throw new ServiceException("packageapi.validation.failure.id", getRequestId());
        attendeeId = Long.parseLong(idStr);

        String pkgName = getRequest().getParameter(PARAM_PACKAGE);
        if (pkgName == null || "".equals(pkgName))
            throw new ServiceException("packageapi.validation.failure.package", getRequestId());

        // check that the package is allowed to be manipulated by this api and token
        if (!allowedPackageName.equals(pkgName))
            throw new ServiceException("security.access.denied", getRequestId());
    }

    protected static class ResponseDTO {
        @JsonProperty("ok")
        public boolean ok = true;

        @JsonProperty("HasPackage")
        public boolean hasPackage = false;
    }

    protected void process() {
        try {
            RequestAuth downstreamAuth = new RequestAuth();
            downstreamAuth.apiToken = getConfiguration().downstream.apiToken;
            attendee = attendeeService.performGetAttendee(attendeeId, downstreamAuth, getRequestId());

            String temp = "," + attendee.packages + ",";
            if (getMethod() == HttpMethod.POST) {
                if (!temp.contains(","+allowedPackageName+",")) {
                    temp += allowedPackageName + ",";
                }
            } else if (getMethod() == HttpMethod.DELETE) {
                if (temp.contains(","+allowedPackageName+",")) {
                    temp = temp.replace(allowedPackageName+",", "");
                }
            }
            temp = temp.replaceFirst("^,", "");
            temp = temp.replaceFirst(",$", "");
            attendee.packages = temp;

            attendeeService.performUpdateAttendee(attendee, downstreamAuth, getRequestId());
        } catch (NotFoundException e) {
            throw new ServiceException("packageapi.attendee.notfound", getRequestId());
        } catch (DownstreamException e) {
            throw new ServiceException("packageapi.downstream.failure", getRequestId());
        }
    }

    protected ResponseDTO createResponse() {
        ResponseDTO response = new ResponseDTO();

        String temp = "," + attendee.packages + ",";
        response.hasPackage = temp.contains(","+allowedPackageName+",");

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
