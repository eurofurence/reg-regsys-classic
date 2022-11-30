package org.eurofurence.regsys.repositories.errors;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ErrorDto {
    @JsonProperty("timestamp")
    public String timestamp;

    @JsonProperty("requestid")
    public String requestid;

    @JsonProperty("message")
    public String message;

    @JsonProperty("details")
    public Map<String, List<String>> details;
}

