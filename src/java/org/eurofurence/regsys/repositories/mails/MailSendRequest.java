package org.eurofurence.regsys.repositories.mails;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MailSendRequest {
    @JsonProperty("cid")
    public String cid;

    @JsonProperty("lang")
    public String lang;

    @JsonProperty("to")
    public List<String> to = new ArrayList<>();

    @JsonProperty("cc")
    public List<String> cc = new ArrayList<>();

    @JsonProperty("bcc")
    public List<String> bcc = new ArrayList<>();

    @JsonProperty("variables")
    public Map<String, String> variables = new TreeMap<>();

    @JsonProperty("async")
    public boolean async;
}
