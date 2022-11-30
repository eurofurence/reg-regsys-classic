package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusChange {
    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("status")
    public String status;

    @JsonProperty("comment")
    public String comment;
}
