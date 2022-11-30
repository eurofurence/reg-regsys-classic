package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BanRule {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("reason")
    public String reason;

    @JsonProperty("name_pattern")
    public String namePattern;

    @JsonProperty("nickname_pattern")
    public String nicknamePattern;

    @JsonProperty("email_pattern")
    public String emailPattern;
}
