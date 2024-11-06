package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Group extends GroupCreate {
    @JsonProperty("id")
    public String id;

    @JsonProperty("members")
    public List<Member> members = new ArrayList<>();

    @JsonProperty("invites")
    public List<Member> invites = new ArrayList<>();
}
