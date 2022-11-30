package org.eurofurence.regsys.repositories.mails;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Template {
    @JsonProperty("uuid")
    public String uuid;

    @JsonProperty("cid")
    public String cid;

    @JsonProperty("lang")
    public String lang;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("data")
    public String data;

    @JsonProperty("created_at")
    public String createdAtISODateTime;

    @JsonProperty("updated_at")
    public String updatedAtISODateTime;
}
