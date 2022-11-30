package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Countdown {
    @JsonProperty("currentTime")
    public String currentTime;

    @JsonProperty("targetTime")
    public String targetTime;

    @JsonProperty("countdown")
    public Long countdown;
}
