package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminInfo {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("flags")
    public String flags;

    @JsonProperty("permissions")
    public String permissions;

    @JsonProperty("admin_comments")
    public String adminComments;

    @JsonProperty("manual_dues")
    public long manualDues;

    @JsonProperty("manual_dues_description")
    public String manualDuesDescription;
}
