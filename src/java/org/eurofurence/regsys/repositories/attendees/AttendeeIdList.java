package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AttendeeIdList {
    @JsonProperty("ids")
    public List<Long> ids;
}
