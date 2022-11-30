package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusOnly {
    @JsonProperty("status")
    public String status;
}
