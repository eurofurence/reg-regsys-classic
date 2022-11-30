package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class BanRuleList {
    @JsonProperty("bans")
    public List<BanRule> bans = new ArrayList<>();
}
