package org.eurofurence.regsys.repositories.mails;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MailPreviewRequest {
    @JsonProperty("cid")
    public String cid;

    @JsonProperty("lang")
    public String lang;

    @JsonProperty("variables")
    public Map<String, String> variables = new TreeMap<>();
}
