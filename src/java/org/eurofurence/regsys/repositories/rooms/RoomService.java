package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.http.client.utils.URIBuilder;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBody;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientNoBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.DownstreamClientWithBodyNoResponse;
import org.eurofurence.regsys.repositories.lowlevel.ResponseWithDto;
import org.eurofurence.regsys.repositories.lowlevel.Utils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RoomService {
    private final String serviceBaseUrl;

    public RoomService() {
        Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();
        serviceBaseUrl = config.downstream.roomService;
    }

    // --- groups ---

    public List<Group> performListGroups(long memberId, boolean showPublic, RequestAuth auth, String requestId) {
        String url;
        try {
            URIBuilder uriBuilder = new URIBuilder(serviceBaseUrl + "/api/rest/v1/groups");
            if (memberId > 0) {
                uriBuilder.addParameter("member_ids", Long.toString(memberId));
            }
            if (showPublic) {
                uriBuilder.addParameter("show", "public");
            }
            url = uriBuilder.toString();
        } catch (URISyntaxException e) {
            throw new DownstreamException("error while building request url", e);
        }

        DownstreamClientNoBody<GroupList> client = new DownstreamClientNoBody<>(GroupList.class);
        ResponseWithDto<GroupList> result = client.performGet(requestId, url,"room/listGroups", auth);
        if (result.dto == null || result.dto.groups == null)
            return new ArrayList<>();
        return result.dto.groups;
    }

    public String performCreateGroup(GroupCreate group, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/groups";
        DownstreamClientWithBodyNoResponse<GroupCreate> client = new DownstreamClientWithBodyNoResponse<>(GroupCreate.class);
        ResponseWithDto<String> result = client.performPost(requestId, url,"room/createGroup", group, auth);
        return Utils.uuidFromLocationHeader(result.location);
    }

    public Group performFindMyGroup(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/groups/my";
        DownstreamClientNoBody<Group> client = new DownstreamClientNoBody<>(Group.class);
        ResponseWithDto<Group> result = client.performGet(requestId, url,"room/findMyGroup", auth);
        return result.dto;
    }

    public Group performGetGroupById(String uuid, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/groups/" + uuid;
        DownstreamClientNoBody<Group> client = new DownstreamClientNoBody<>(Group.class);
        ResponseWithDto<Group> result = client.performGet(requestId, url,"room/getGroupById", auth);
        return result.dto;
    }

    public String performUpdateGroup(Group group, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/groups/" + group.id;
        DownstreamClientWithBodyNoResponse<Group> client = new DownstreamClientWithBodyNoResponse<>(Group.class);
        ResponseWithDto<String> result = client.performPut(requestId, url,"room/updateGroup", group, auth);
        return Utils.uuidFromLocationHeader(result.location);
    }

    public void performDeleteGroup(String uuid, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/groups/" + uuid;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"room/deleteGroup", auth);
    }

    // returns join link if an invitation was created
    public String performAddToGroup(String uuid, long badgeNumber, String nickname, String code, boolean force, RequestAuth auth, String requestId) {
        String url;
        try {
            URIBuilder uriBuilder = new URIBuilder(serviceBaseUrl + "/api/rest/v1/groups/" + uuid + "/members/" + badgeNumber);
            if (nonEmpty(nickname)) {
                uriBuilder.addParameter("nickname", nickname);
            }
            if (nonEmpty(code)) {
                uriBuilder.addParameter("code", code);
            }
            if (force) {
                uriBuilder.addParameter("force", "true");
            }
            url = uriBuilder.toString();
        } catch (URISyntaxException e) {
            throw new DownstreamException("error while building request url", e);
        }

        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        ResponseWithDto<String> result = client.performPost(requestId, url,"room/addToGroup", auth);
        return result.location;
    }

    public void performRemoveFromGroup(String uuid, long badgeNumber, boolean autodeny, RequestAuth auth, String requestId) {
        String url;
        try {
            URIBuilder uriBuilder = new URIBuilder(serviceBaseUrl + "/api/rest/v1/groups/" + uuid + "/members/" + badgeNumber);
            if (autodeny) {
                uriBuilder.addParameter("autodeny", "true");
            }
            url = uriBuilder.toString();
        } catch (URISyntaxException e) {
            throw new DownstreamException("error while building request url", e);
        }

        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"room/removeFromGroup", auth);
    }

    // --- rooms ---

    public List<Room> performListRooms(long occupantId, RequestAuth auth, String requestId) {
        String url;
        try {
            URIBuilder uriBuilder = new URIBuilder(serviceBaseUrl + "/api/rest/v1/rooms");
            if (occupantId > 0) {
                uriBuilder.addParameter("occupant_ids", Long.toString(occupantId));
            }
            url = uriBuilder.toString();
        } catch (URISyntaxException e) {
            throw new DownstreamException("error while building request url", e);
        }

        DownstreamClientNoBody<RoomList> client = new DownstreamClientNoBody<>(RoomList.class);
        ResponseWithDto<RoomList> result = client.performGet(requestId, url,"room/listRooms", auth);
        if (result.dto == null || result.dto.rooms == null)
            return new ArrayList<>();
        return result.dto.rooms;
    }

    public String performCreateRoom(RoomCreate room, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/rooms";
        DownstreamClientWithBodyNoResponse<RoomCreate> client = new DownstreamClientWithBodyNoResponse<>(RoomCreate.class);
        ResponseWithDto<String> result = client.performPost(requestId, url,"room/createRoom", room, auth);
        return Utils.uuidFromLocationHeader(result.location);
    }

    public Room performFindMyRoom(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/rooms/my";
        DownstreamClientNoBody<Room> client = new DownstreamClientNoBody<>(Room.class);
        ResponseWithDto<Room> result = client.performGet(requestId, url,"room/findMyRoom", auth);
        return result.dto;
    }

    public Room performGetRoomById(String uuid, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/rooms/" + uuid;
        DownstreamClientNoBody<Room> client = new DownstreamClientNoBody<>(Room.class);
        ResponseWithDto<Room> result = client.performGet(requestId, url,"room/getRoomById", auth);
        return result.dto;
    }

    public String performUpdateRoom(Room room, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/rooms/" + room.id;
        DownstreamClientWithBodyNoResponse<Room> client = new DownstreamClientWithBodyNoResponse<>(Room.class);
        ResponseWithDto<String> result = client.performPut(requestId, url,"room/updateRoom", room, auth);
        return Utils.uuidFromLocationHeader(result.location);
    }

    public void performDeleteRoom(String uuid, RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/rooms/" + uuid;
        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"room/deleteRoom", auth);
    }

    public void performAddToRoom(String uuid, long badgeNumber, RequestAuth auth, String requestId) {
        String url;
        try {
            URIBuilder uriBuilder = new URIBuilder(serviceBaseUrl + "/api/rest/v1/rooms/" + uuid + "/occupants/" + badgeNumber);
            url = uriBuilder.toString();
        } catch (URISyntaxException e) {
            throw new DownstreamException("error while building request url", e);
        }

        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performPost(requestId, url,"room/addToRoom", auth);
    }

    public void performRemoveFromRoom(String uuid, long badgeNumber, RequestAuth auth, String requestId) {
        String url;
        try {
            URIBuilder uriBuilder = new URIBuilder(serviceBaseUrl + "/api/rest/v1/rooms/" + uuid + "/occupants/" + badgeNumber);
            url = uriBuilder.toString();
        } catch (URISyntaxException e) {
            throw new DownstreamException("error while building request url", e);
        }

        DownstreamClientNoBodyNoResponse client = new DownstreamClientNoBodyNoResponse();
        client.performDelete(requestId, url,"room/removeFromRoom", auth);
    }

    // --- countdown ---

    public Countdown performGetCountdown(RequestAuth auth, String requestId) {
        String url = serviceBaseUrl + "/api/rest/v1/countdown";
        DownstreamClientNoBody<Countdown> client = new DownstreamClientNoBody<>(Countdown.class);
        ResponseWithDto<Countdown> result = client.performGet(requestId, url,"room/countdown", auth);
        return result.dto;
    }

    // --- internals ---

    protected boolean nonEmpty(String str) {
        return str != null && !"".equals(str);
    }

    protected static class GroupList {
        @JsonProperty("groups")
        public List<Group> groups = new ArrayList<>();
    }

    protected static class RoomList {
        @JsonProperty("rooms")
        public List<Room> rooms = new ArrayList<>();
    }
}
