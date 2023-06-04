package org.eurofurence.regsys.repositories.attendees;

import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.errors.ForbiddenException;
import org.eurofurence.regsys.repositories.errors.NotFoundException;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBody;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientWithBody;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientWithBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.ResponseWithDto;
import org.eurofurence.regsys.repositories.lowlevel.Utils;

import java.util.ArrayList;
import java.util.List;

public class AttendeeService {
    private final String serviceBaseUrl;

    public AttendeeService() {
        Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        serviceBaseUrl = config.downstream.attendeeService;
    }

    // --- registration ---

    public List<Long> performGetBadgeNumbers(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees";
        DownstreamClientNoBody<AttendeeIdList> client = new DownstreamClientNoBody<>(AttendeeIdList.class);
        try {
            ResponseWithDto<AttendeeIdList> result = client.performGet(requestId, url,"attendee/listMyRegistrations", auth);
            if (result.dto == null || result.dto.ids == null)
                return new ArrayList<>();
            return result.dto.ids;
        } catch (NotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Attendee performGetAttendee(long id, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id;
        DownstreamClientNoBody<Attendee> client = new DownstreamClientNoBody<>(Attendee.class);
        ResponseWithDto<Attendee> result = client.performGet(requestId, url,"attendee/getAttendee", auth);
        return result.dto;
    }

    public long performAddAttendee(Attendee attendee, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees";
        DownstreamClientWithBodyNoResponse<Attendee> client = new DownstreamClientWithBodyNoResponse<>(Attendee.class);
        ResponseWithDto<String> result = client.performPost(requestId, url,"attendee/addAttendee", attendee, auth);
        return Utils.idFromLocationHeader(result.location);
    }

    public void performUpdateAttendee(Attendee attendee, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendee.id;
        DownstreamClientWithBodyNoResponse<Attendee> client = new DownstreamClientWithBodyNoResponse<>(Attendee.class);
        client.performPut(requestId, url,"attendee/updateAttendee", attendee, auth);
    }

    public void performUpdateAttendeeWithoutEmail(Attendee attendee, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendee.id + "?suppressMinorUpdateEmail=yes";
        DownstreamClientWithBodyNoResponse<Attendee> client = new DownstreamClientWithBodyNoResponse<>(Attendee.class);
        client.performPut(requestId, url,"attendee/updateAttendeeNoEmail", attendee, auth);
    }

    // --- additional ---

    public AddInfo performGetAdditionalInfo(long attendeeId, String area, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/additional-info/" + area;
        DownstreamClientNoBody<AddInfo> client = new DownstreamClientNoBody<>(AddInfo.class);
        ResponseWithDto<AddInfo> result = client.performGet(requestId, url,"attendee/getAdditionalInfo", auth);
        return result.dto;
    }

    public void performSetAdditionalInfo(long attendeeId, String area, AddInfo value, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/additional-info/" + area;
        DownstreamClientWithBodyNoResponse<AddInfo> client = new DownstreamClientWithBodyNoResponse<>(AddInfo.class);
        client.performPost(requestId, url,"attendee/setAdditionalInfo", value, auth);
    }

    public void performDeleteAdditionalInfo(long attendeeId, String area, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/additional-info/" + area;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"attendee/deleteAdditionalInfo", auth);
    }

    // --- due date ---

    public String performGetDueDate(long id, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id + "/due-date";
        DownstreamClientNoBody<DueDate> client = new DownstreamClientNoBody<>(DueDate.class);
        ResponseWithDto<DueDate> result = client.performGet(requestId, url,"attendee/getDueDate", auth);
        if (result.dto != null) {
            return result.dto.dueDate;
        } else {
            return "";
        }
    }

    public void performOverrideDueDate(long id, String dueDate, RequestAuth auth, String requestId) {
        DueDate body = new DueDate();
        body.dueDate = dueDate;
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id + "/due-date";
        DownstreamClientWithBodyNoResponse<DueDate> client = new DownstreamClientWithBodyNoResponse<>(DueDate.class);
        client.performPut(requestId, url,"attendee/overrideDueDate", body, auth);
    }

    // --- status ---

    public String performGetCurrentStatus(long id, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id + "/status";
        DownstreamClientNoBody<StatusOnly> client = new DownstreamClientNoBody<>(StatusOnly.class);
        ResponseWithDto<StatusOnly> result = client.performGet(requestId, url,"attendee/getStatus", auth);
        return result.dto.status;
    }

    public void performStatusChange(long attendeeId, StatusChange change, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/status";
        DownstreamClientWithBodyNoResponse<StatusChange> client = new DownstreamClientWithBodyNoResponse<>(StatusChange.class);
        client.performPost(requestId, url,"attendee/changeStatus", change, auth);
    }

    public StatusHistory performGetStatusHistory(long id, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id + "/status-history";
        DownstreamClientNoBody<StatusHistory> client = new DownstreamClientNoBody<>(StatusHistory.class);
        ResponseWithDto<StatusHistory> result = client.performGet(requestId, url,"attendee/getStatusHistory", auth);
        return result.dto;
    }

    public void performResendStatusEmail(long attendeeId, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/status/resend";
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performPost(requestId, url,"attendee/resendStatusMail", auth);
    }

    // --- privileged: adminInfo ---

    public AdminInfo performGetAdminInfo(long attendeeId, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/admin";
        DownstreamClientNoBody<AdminInfo> client = new DownstreamClientNoBody<>(AdminInfo.class);
        ResponseWithDto<AdminInfo> result = client.performGet(requestId, url,"attendee/getAdminInfo", auth);
        return result.dto;
    }

    public void performSetAdminInfo(long attendeeId, AdminInfo value, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/admin";
        DownstreamClientWithBodyNoResponse<AdminInfo> client = new DownstreamClientWithBodyNoResponse<>(AdminInfo.class);
        client.performPut(requestId, url,"attendee/setAdminInfo", value, auth);
    }

    // --- privileged: search ---

    public AttendeeSearchResultList performFindAttendees(AttendeeSearchCriteria criteria, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/find";
        DownstreamClientWithBody<AttendeeSearchCriteria, AttendeeSearchResultList> client = new DownstreamClientWithBody<>(AttendeeSearchCriteria.class, AttendeeSearchResultList.class);
        ResponseWithDto<AttendeeSearchResultList> result = client.performPost(requestId, url,"attendee/findAttendees", criteria, auth);
        return result.dto;
    }

    // --- privileged: bans ---

    public BanRuleList performListBans(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans";
        DownstreamClientNoBody<BanRuleList> client = new DownstreamClientNoBody<>(BanRuleList.class);
        ResponseWithDto<BanRuleList> result = client.performGet(requestId, url,"attendee/listBans", auth);
        return result.dto;
    }

    public long performAddBan(BanRule ban, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans";
        DownstreamClientWithBodyNoResponse<BanRule> client = new DownstreamClientWithBodyNoResponse<>(BanRule.class);
        ResponseWithDto<String> result = client.performPost(requestId, url,"attendee/addBan", ban, auth);
        return Utils.idFromLocationHeader(result.location);
    }

    public BanRule performGetBan(long id, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans/" + id;
        DownstreamClientNoBody<BanRule> client = new DownstreamClientNoBody<>(BanRule.class);
        ResponseWithDto<BanRule> result = client.performGet(requestId, url,"attendee/getBan", auth);
        return result.dto;
    }

    public void performUpdateBan(BanRule ban, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans/" + ban.id;
        DownstreamClientWithBodyNoResponse<BanRule> client = new DownstreamClientWithBodyNoResponse<>(BanRule.class);
        client.performPut(requestId, url,"attendee/updateBan", ban, auth);
    }

    public void performDeleteBan(long id, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans/" + id;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"attendee/deleteBan", auth);
    }

    // --- info ---

    public Countdown performGetCountdown(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/countdown";
        DownstreamClientNoBody<Countdown> client = new DownstreamClientNoBody<>(Countdown.class);
        ResponseWithDto<Countdown> result = client.performGet(requestId, url,"attendee/countdown", auth);
        return result.dto;
    }
}
