package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class StatusHistory {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("status_history")
    public List<StatusChange> statusHistory = new ArrayList<>();
}
