package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class RoomCreate {
    @JsonProperty("name")
    public String name;

    @JsonProperty("flags")
    public List<String> flags = new ArrayList<>();

    @JsonProperty("comments")
    public String comments;

    @JsonProperty("size")
    public long size;
}
