package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GroupCreate {
    @JsonProperty("name")
    public String name;

    @JsonProperty("flags")
    public List<String> flags = new ArrayList<>();

    @JsonProperty("comments")
    public String comments;

    @JsonProperty("maximum_size")
    public long maximumSize;

    @JsonProperty("owner")
    public long owner;
}
