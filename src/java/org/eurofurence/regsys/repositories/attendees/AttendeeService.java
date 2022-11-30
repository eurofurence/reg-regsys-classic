package org.eurofurence.regsys.repositories.attendees;

import org.eurofurence.regsys.backend.HardcodedConfig;
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

    public List<Long> performGetBadgeNumbers(String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees";
        DownstreamClientNoBody<AttendeeIdList> client = new DownstreamClientNoBody<>(AttendeeIdList.class);
        try {
            ResponseWithDto<AttendeeIdList> result = client.performGet(requestId, url,"attendee/listMyRegistrations", token);
            if (result.dto == null || result.dto.ids == null)
                return new ArrayList<>();
            return result.dto.ids;
        } catch (NotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Attendee performGetAttendee(long id, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id;
        DownstreamClientNoBody<Attendee> client = new DownstreamClientNoBody<>(Attendee.class);
        ResponseWithDto<Attendee> result = client.performGet(requestId, url,"attendee/getAttendee", token);
        return result.dto;
    }

    public long performAddAttendee(Attendee attendee, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees";
        DownstreamClientWithBodyNoResponse<Attendee> client = new DownstreamClientWithBodyNoResponse<>(Attendee.class);
        ResponseWithDto<String> result = client.performPost(requestId, url,"attendee/addAttendee", attendee, token);
        return Utils.idFromLocationHeader(result.location);
    }

    public void performUpdateAttendee(Attendee attendee, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendee.id;
        DownstreamClientWithBodyNoResponse<Attendee> client = new DownstreamClientWithBodyNoResponse<>(Attendee.class);
        client.performPut(requestId, url,"attendee/updateAttendee", attendee, token);
    }

    // --- additional ---

    public AddInfo performGetAdditionalInfo(long attendeeId, String area, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/additional-info/" + area;
        DownstreamClientNoBody<AddInfo> client = new DownstreamClientNoBody<>(AddInfo.class);
        ResponseWithDto<AddInfo> result = client.performGet(requestId, url,"attendee/getAdditionalInfo", token);
        return result.dto;
    }

    public void performSetAdditionalInfo(long attendeeId, String area, AddInfo value, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/additional-info/" + area;
        DownstreamClientWithBodyNoResponse<AddInfo> client = new DownstreamClientWithBodyNoResponse<>(AddInfo.class);
        client.performPost(requestId, url,"attendee/setAdditionalInfo", value, token);
    }

    public void performDeleteAdditionalInfo(long attendeeId, String area, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/additional-info/" + area;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"attendee/deleteAdditionalInfo", token);
    }

    // --- status ---

    public String performGetCurrentStatus(long id, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id + "/status";
        DownstreamClientNoBody<StatusOnly> client = new DownstreamClientNoBody<>(StatusOnly.class);
        ResponseWithDto<StatusOnly> result = client.performGet(requestId, url,"attendee/getStatus", token);
        return result.dto.status;
    }

    public void performStatusChange(long attendeeId, StatusChange change, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/status";
        DownstreamClientWithBodyNoResponse<StatusChange> client = new DownstreamClientWithBodyNoResponse<>(StatusChange.class);
        client.performPost(requestId, url,"attendee/changeStatus", change, token);
    }

    public StatusHistory performGetStatusHistory(long id, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + id + "/status-history";
        DownstreamClientNoBody<StatusHistory> client = new DownstreamClientNoBody<>(StatusHistory.class);
        ResponseWithDto<StatusHistory> result = client.performGet(requestId, url,"attendee/getStatusHistory", token);
        return result.dto;
    }

    // --- privileged: adminInfo ---

    public AdminInfo performGetAdminInfo(long attendeeId, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/admin";
        DownstreamClientNoBody<AdminInfo> client = new DownstreamClientNoBody<>(AdminInfo.class);
        ResponseWithDto<AdminInfo> result = client.performGet(requestId, url,"attendee/getAdminInfo", token);
        return result.dto;
    }

    public void performSetAdminInfo(long attendeeId, String area, AddInfo value, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/" + attendeeId + "/admin";
        DownstreamClientWithBodyNoResponse<AddInfo> client = new DownstreamClientWithBodyNoResponse<>(AddInfo.class);
        client.performPut(requestId, url,"attendee/setAdminInfo", value, token);
    }

    // --- privileged: search ---

    public AttendeeSearchResultList performFindAttendees(AttendeeSearchCriteria criteria, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/attendees/find";
        DownstreamClientWithBody<AttendeeSearchCriteria, AttendeeSearchResultList> client = new DownstreamClientWithBody<>(AttendeeSearchCriteria.class, AttendeeSearchResultList.class);
        ResponseWithDto<AttendeeSearchResultList> result = client.performPost(requestId, url,"attendee/findAttendees", criteria, token);
        return result.dto;
    }

    // --- privileged: bans ---

    public BanRuleList performListBans(String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans";
        DownstreamClientNoBody<BanRuleList> client = new DownstreamClientNoBody<>(BanRuleList.class);
        ResponseWithDto<BanRuleList> result = client.performGet(requestId, url,"attendee/listBans", token);
        return result.dto;
    }

    public long performAddBan(BanRule ban, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans";
        DownstreamClientWithBodyNoResponse<BanRule> client = new DownstreamClientWithBodyNoResponse<>(BanRule.class);
        ResponseWithDto<String> result = client.performPost(requestId, url,"attendee/addBan", ban, token);
        return Utils.idFromLocationHeader(result.location);
    }

    public BanRule performGetBan(long id, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans/" + id;
        DownstreamClientNoBody<BanRule> client = new DownstreamClientNoBody<>(BanRule.class);
        ResponseWithDto<BanRule> result = client.performGet(requestId, url,"attendee/getBan", token);
        return result.dto;
    }

    public void performUpdateBan(BanRule ban, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans/" + ban.id;
        DownstreamClientWithBodyNoResponse<BanRule> client = new DownstreamClientWithBodyNoResponse<>(BanRule.class);
        client.performPut(requestId, url,"attendee/updateBan", ban, token);
    }

    public void performDeleteBan(long id, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/bans/" + id;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"attendee/deleteBan", token);
    }

    // --- info ---

    public Countdown performGetCountdown(String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/countdown";
        DownstreamClientNoBody<Countdown> client = new DownstreamClientNoBody<>(Countdown.class);
        ResponseWithDto<Countdown> result = client.performGet(requestId, url,"attendee/countdown", token);
        return result.dto;
    }

    // --- unofficial - this may move to auth service at some point ---

    public boolean performHasRole(String role, String token, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/roles/" + role;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        try {
            ResponseWithDto<String> result = client.performGet(requestId, url,"attendee/roles", token);
        } catch (UnauthorizedException | ForbiddenException e) {
            return false;
        }
        return true;
    }
}
