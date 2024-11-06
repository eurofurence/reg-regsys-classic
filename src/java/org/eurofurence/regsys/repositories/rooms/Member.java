package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Member {
    @JsonProperty("id")
    public long id;

    @JsonProperty("nickname")
    public String nickname;

    @JsonProperty("avatar")
    public String avatar;

    @JsonProperty("flags")
    public List<String> flags = new ArrayList<>();
}
