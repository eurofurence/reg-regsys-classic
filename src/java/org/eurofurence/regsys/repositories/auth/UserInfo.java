package org.eurofurence.regsys.repositories.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    @JsonProperty("audiences")
    public List<String> audiences = new ArrayList<>();

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("name")
    public String name;

    @JsonProperty("email")
    public String email;

    @JsonProperty("email_verified")
    public boolean emailVerified;

    @JsonProperty("groups")
    public List<String> groups = new ArrayList<>();
}
