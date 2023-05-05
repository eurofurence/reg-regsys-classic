package org.eurofurence.regsys.web.services;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.config.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SummerBoatApi extends AbstractAttendeeListService {
    // --- parameter constants ---

    private static final String PARAM_TOKEN   = "token";
    private static final String PARAM_ID      = "id";

    // --- response structure ---

    protected static class ListResponseDTO extends AbstractResponseDTO {
        public List<SummerBoatInfoDto> result = new LinkedList<>();
    }

    protected static class SummerBoatInfoDto {
        public long id;
        public String nick;
        public String email;
        public String status;
        public List<String> packages;
        public String lastName;
        public String firstName;
        public String birthday;
    }

    // ------------------------ mapper -----------------------------------------

    private SummerBoatInfoDto infoFromAttendee(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        SummerBoatInfoDto info = new SummerBoatInfoDto();
        info.id = attendee.id;
        info.nick = attendee.nickname;
        info.status = attendee.status;
        info.email = attendee.email;
        info.packages = Arrays.stream(attendee.packages.split(",")).collect(Collectors.toList());
        info.lastName = attendee.lastName;
        info.firstName = attendee.firstName;
        info.birthday = attendee.birthday;
        return info;
    }

    // -------------------- implementation -------------------------------------

    private long singleId = -1;

    @Override
    protected void finderAdditionalSetup(AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion) {
        criterion.status = Arrays.asList("new", "approved", "partially paid", "paid", "checked in", "waiting", "cancelled");

        if (singleId < -1) {
            throw new ServiceException("summerboat.validation.failure.id", getRequestId());
        }
        if (singleId < 1) {
            return;
        }

        criterion.ids = Collections.singletonList(singleId);
    }

    private boolean validTokenMatch(String token, String allowedToken) {
        if (token != null && allowedToken != null && !"".equals(allowedToken)) {
            return allowedToken.equals(token);
        }
        return false;
    }

    @Override
    protected void authenticate() {
        String token = getRequest().getParameter(PARAM_TOKEN);
        Configuration.DownstreamConfig downstream = getConfiguration().downstream;
        if (downstream != null) {
            if (validTokenMatch(token, downstream.boatToken)) {
                return;
            }
        }

        throw new ServiceException("security.invalid.token", getRequestId());
    }

    @Override
    protected AbstractResponseDTO createResponse() {
        String idStr = getRequest().getParameter(PARAM_ID);
        ListResponseDTO output = new ListResponseDTO();
        if (idStr != null) {
            if (!idStr.matches("[1-9][0-9]*"))
                throw new ServiceException("summerboat.validation.failure.id", getRequestId());
            singleId = Long.parseLong(idStr);
        }
        loopOverAllAttendees(a -> output.result.add(infoFromAttendee(a)), a -> true, "summerboat.dberror.find.attendee.failure");
        return output;
    }
}
