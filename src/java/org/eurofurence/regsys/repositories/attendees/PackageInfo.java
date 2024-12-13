package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PackageInfo {
    @JsonProperty("name")
    public String name;

    @JsonProperty("count")
    public long count;
}
