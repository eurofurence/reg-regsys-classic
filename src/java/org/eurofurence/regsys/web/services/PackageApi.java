package org.eurofurence.regsys.web.services;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String packageName = "";
    private Set<String> allowedPackageNames = new HashSet<>();

    private Attendee attendee = new Attendee();
    private final AttendeeService attendeeService = new AttendeeService();

    // --- auth ---

    protected Map<String,String> authForPackage() {
        Map<String,String> allowed = new HashMap<>();

        Configuration.DownstreamConfig downstream = getConfiguration().downstream;
        if (downstream != null) {
            // TODO: hardcoded for now
            if (downstream.ddToken != null && !"".equals(downstream.ddToken)) {
                allowed.put("dealer-half", downstream.ddToken);
                allowed.put("dealer-full", downstream.ddToken);
                allowed.put("dealer-fullplus", downstream.ddToken);
                allowed.put("dealer-double", downstream.ddToken);
                allowed.put("dealer-quad", downstream.ddToken);
            }
            if (downstream.boatToken != null && !"".equals(downstream.boatToken)) {
                allowed.put("boat-trip", downstream.boatToken);
                allowed.put("boat-vip", downstream.boatToken);
                allowed.put("boat-benefactor", downstream.boatToken);
            }
            if (downstream.artshowToken != null && !"".equals(downstream.artshowToken)) {
                allowed.put("artshow-table-half", downstream.artshowToken);
                allowed.put("artshow-table-one", downstream.artshowToken);
                allowed.put("artshow-table-oneandhalf", downstream.artshowToken);
                allowed.put("artshow-table-two", downstream.artshowToken);
                allowed.put("artshow-table-twoandhalf", downstream.artshowToken);
                allowed.put("artshow-table-three", downstream.artshowToken);
                allowed.put("artshow-table-threeandhalf", downstream.artshowToken);
                allowed.put("artshow-table-four", downstream.artshowToken);
                allowed.put("artshow-panel-half", downstream.artshowToken);
                allowed.put("artshow-panel-one", downstream.artshowToken);
                allowed.put("artshow-panel-oneandhalf", downstream.artshowToken);
                allowed.put("artshow-panel-two", downstream.artshowToken);
                allowed.put("artshow-panel-twoandhalf", downstream.artshowToken);
                allowed.put("artshow-panel-three", downstream.artshowToken);
                allowed.put("artshow-panel-threeandhalf", downstream.artshowToken);
                allowed.put("artshow-panel-four", downstream.artshowToken);
            }
        }
        return allowed;
    }

    protected void authenticate() {
        String token = getRequest().getParameter(PARAM_TOKEN);
        if (token == null || "".equals(token))
            throw new ServiceException("security.invalid.token", getRequestId());

        allowedPackageNames = authForPackage().entrySet().stream()
                .filter(e -> e.getValue().equals(token))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (allowedPackageNames.isEmpty())
            throw new ServiceException("security.invalid.token", getRequestId());
    }

    // --- parameters and preparations ---

    protected void parseParameters() {
        String idStr = getRequest().getParameter(PARAM_ID);
        if ((idStr == null) || !idStr.matches("[1-9][0-9]*"))
            throw new ServiceException("packageapi.validation.failure.id", getRequestId());
        attendeeId = Long.parseLong(idStr);

        if (getMethod() == HttpMethod.GET) {
            packageName = "";
        } else {
            String pkgName = getRequest().getParameter(PARAM_PACKAGE);
            if (pkgName == null || "".equals(pkgName))
                throw new ServiceException("packageapi.validation.failure.package", getRequestId());

            // check that the package is allowed to be manipulated by this api and token
            if (!allowedPackageNames.contains(pkgName))
                throw new ServiceException("security.access.denied", getRequestId());

            packageName = pkgName;
        }
    }

    protected static class ResponseDTO {
        @JsonProperty("ok")
        public boolean ok = true;

        @JsonProperty("packages")
        public List<String> packages = new ArrayList<>();
    }

    protected void process() {
        try {
            RequestAuth downstreamAuth = new RequestAuth();
            downstreamAuth.apiToken = getConfiguration().downstream.apiToken;
            attendee = attendeeService.performGetAttendee(attendeeId, downstreamAuth, getRequestId());

            boolean updated = false;

            String temp = "," + attendee.packages + ",";
            if (getMethod() == HttpMethod.POST) {
                if (!temp.contains(","+packageName+",")) {
                    temp += packageName + ",";
                    updated = true;
                }
            } else if (getMethod() == HttpMethod.DELETE) {
                if (temp.contains(","+packageName+",")) {
                    temp = temp.replace(packageName+",", "");
                    updated = true;
                }
            }

            if (updated) {
                temp = temp.replaceFirst("^,", "");
                temp = temp.replaceFirst(",$", "");
                attendee.packages = temp;

                attendeeService.performUpdateAttendee(attendee, downstreamAuth, getRequestId());
            }
        } catch (NotFoundException e) {
            throw new ServiceException("packageapi.attendee.notfound", getRequestId());
        } catch (DownstreamException e) {
            throw new ServiceException("packageapi.downstream.failure", getRequestId());
        }
    }

    protected ResponseDTO createResponse() {
        ResponseDTO response = new ResponseDTO();

        String temp = "," + attendee.packages + ",";
        response.packages = allowedPackageNames.stream()
                .filter(n -> temp.contains(","+n+","))
                .sorted().collect(Collectors.toList());

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
