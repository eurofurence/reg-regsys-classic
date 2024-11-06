package org.eurofurence.regsys.repositories.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Countdown {
    @JsonProperty("currentTime")
    public String currentTimeIsoDateTime;

    @JsonProperty("targetTime")
    public String targetTimeIsoDateTime;

    @JsonProperty("countdown")
    public long countdownSeconds;

    @JsonProperty("secret")
    public String secret;
}
