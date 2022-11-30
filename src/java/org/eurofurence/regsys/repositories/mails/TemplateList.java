package org.eurofurence.regsys.repositories.mails;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TemplateList {
    @JsonProperty("templates")
    public List<Template> templates = new ArrayList<>();
}
