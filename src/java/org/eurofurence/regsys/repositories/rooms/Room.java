package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Room extends RoomCreate {
    @JsonProperty("id")
    public String id;

    @JsonProperty("occupants")
    public List<Member> occupants = new ArrayList<>();
}
