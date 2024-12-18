package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendeeSearchCriteria {
    public static class AttendeeSearchSingleCriterion {
        @JsonProperty("ids")
        public List<Long> ids;

        @JsonProperty("nickname")
        public String nickname;

        @JsonProperty("name")
        public String name;

        @JsonProperty("address")
        public String address;

        @JsonProperty("country")
        public String country;

        @JsonProperty("email")
        public String email;

        @JsonProperty("telegram")
        public String telegram;

        @JsonProperty("spoken_languages")
        public Map<String, Integer> spokenLanguages = new HashMap<>();

        @JsonProperty("registration_language")
        public Map<String, Integer> registrationLanguage = new HashMap<>();

        @JsonProperty("flags")
        public Map<String, Integer> flags = new HashMap<>();

        @JsonProperty("options")
        public Map<String, Integer> options = new HashMap<>();

        @JsonProperty("packages")
        public Map<String, Integer> packages = new HashMap<>();

        @JsonProperty("user_comments")
        public String userComments;

        @JsonProperty("status")
        public List<String> status;

        @JsonProperty("permissions")
        public Map<String, Integer> permissions = new HashMap<>();

        @JsonProperty("admin_comments")
        public String adminComments;

        @JsonProperty("add_info")
        public Map<String, Integer> addInfo = new HashMap<>();

        @JsonProperty("identity_subjects")
        public List<String> identitySubjects = new ArrayList<>();
    }

    @JsonProperty("match_any")
    public List<AttendeeSearchSingleCriterion> matchAny = new ArrayList<>();

    @JsonProperty("min_id")
    public long minId;

    @JsonProperty("max_id")
    public long maxId;

    @JsonProperty("num_results")
    public long numResults;

    @JsonProperty("fill_fields")
    public List<String> fillFields;

    @JsonProperty("sort_by")
    public String sortBy;

    @JsonProperty("sort_order")
    public String sortOrder;
}
