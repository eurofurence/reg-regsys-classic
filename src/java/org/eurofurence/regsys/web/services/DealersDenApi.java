package org.eurofurence.regsys.web.services;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.config.Configuration;

public class DealersDenApi extends AbstractAttendeeListService {
    protected static class ListResponseDTO extends AbstractResponseDTO {
        public List<DealersDenInfoDTO> result = new LinkedList<>();
    }

    protected static class DealersDenInfoDTO {
        public long id;
        public String nick;
        public String email;
        public String status;
    }

    // ------------------------ mapper -----------------------------------------

    private DealersDenInfoDTO infoFromAttendee(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        DealersDenInfoDTO info = new DealersDenInfoDTO();
        info.id = attendee.id;
        info.nick = attendee.nickname;
        info.status = attendee.status;
        info.email = attendee.email;
        return info;
    }

    // -------------------- implementation -------------------------------------

    private long singleId = -1;

    @Override
    protected void finderAdditionalSetup(AttendeeSearchCriteria.AttendeeSearchSingleCriterion criterion) {
        if (singleId < 1) {
            throw new ServiceException("dealersden.validation.failure.id", getRequestId());
        }
        criterion.ids = Collections.singletonList(singleId);
    }

    @Override
    protected void authenticate() {
        String token = getRequest().getParameter("token");

        Configuration.DownstreamConfig downstream = getConfiguration().downstream;
        if (downstream == null || downstream.ddToken == null || "".equals(downstream.ddToken)) {
            throw new ServiceException("security.invalid.token", getRequestId());
        }
        if (!downstream.ddToken.equals(token)) {
            throw new ServiceException("security.invalid.token", getRequestId());
        }
    }

    @Override
    protected AbstractResponseDTO createResponse() {
        String idStr = getRequest().getParameter("id");
        ListResponseDTO output = new ListResponseDTO();
        if (idStr != null) {
            if (!idStr.matches("[1-9][0-9]*"))
                throw new ServiceException("dealersden.validation.failure.id", getRequestId());
            singleId = Long.parseLong(idStr);
        }
        loopOverAllAttendees(a -> output.result.add(infoFromAttendee(a)), a -> true, "dealersden.dberror.find.attendee.failure");
        return output;
    }
}
