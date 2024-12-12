package org.eurofurence.regsys.repositories.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Configuration {
    public static class ChoiceConfig {
        @JsonProperty("allowed_counts")
        public List<Integer> allowedCounts = new ArrayList<>();

        @JsonProperty("max_count")
        public int maxCount;

        @JsonProperty("description")
        public String description;

        @JsonProperty("price")
        public int price;

        @JsonProperty("vat_percent")
        public float vatPercent;

        @JsonProperty("default")
        public boolean defaultValue;

        @JsonProperty("admin_only")
        public boolean adminOnly;

        @JsonProperty("read_only")
        public boolean readOnly;

        @JsonProperty("at-least-one-mandatory")
        public boolean atLeastOneMandatory; // unused but helm chart creates it

        @JsonProperty("constraint")
        public String constraint;

        @JsonProperty("constraint_msg")
        public String constraintMsg;

        @JsonProperty("visible_for")
        public List<String> visibleFor = new ArrayList<>(); // unused but helm chart creates it

        @JsonProperty("group")
        public String group; // unused but helm chart creates it
    }

    public static class Choices {
        @JsonProperty("flags")
        public Map<String,ChoiceConfig> flags;

        @JsonProperty("packages")
        public Map<String,ChoiceConfig> packages;

        @JsonProperty("options")
        public Map<String,ChoiceConfig> options;
    }

    public static class DownstreamConfig {
        @JsonProperty("auth_service")
        public String authService;

        @JsonProperty("attendee_service")
        public String attendeeService;

        @JsonProperty("mail_service")
        public String mailService;

        @JsonProperty("payment_service")
        public String paymentService;

        @JsonProperty("room_service")
        public String roomService;

        @JsonProperty("id_token_cookie_name")
        public String idTokenCookieName;

        @JsonProperty("access_token_cookie_name")
        public String accessTokenCookieName;

        @JsonProperty("api_token")
        public String apiToken; // needed for the interfaces

        @JsonProperty("admin_group")
        public String adminGroup;

        @JsonProperty("nosecounter_token")
        public String nosecounterToken; // needed for the interfaces

        @JsonProperty("dd_token")
        public String ddToken;

        @JsonProperty("boat_token")
        public String boatToken;

        @JsonProperty("artshow_token")
        public String artshowToken;

        @JsonProperty("secu_token")
        public String secuToken;

        @JsonProperty("secu_secret")
        public String secuSecret;
    }

    public static class TestingConfig {
        @JsonProperty("admin_token")
        public String adminToken;

        @JsonProperty("staff_token")
        public String staffToken;

        @JsonProperty("user_token")
        public String userToken;
    }

    public static class WebConfig {
        @JsonProperty("server_url")
        public String serverUrl; // with trailing slash

        @JsonProperty("context_path")
        public String contextPath; // no slashes

        @JsonProperty("regsys_public_url")
        public String regsysPublicUrl; // full url

        @JsonProperty("enable_dev_cors_header")
        public String enableDevCorsHeader; // set to "true" to enable this, NOT FOR PRODUCTION

        @JsonProperty("session_timeout_seconds")
        public int sessionTimeout = 3600;

        @JsonProperty("enable_registration")
        public boolean enableRegistration = true;

        @JsonProperty("readonly_except_admin")
        public boolean readonlyExceptAdmin;

        @JsonProperty("before_convention_show_announcement")
        public boolean beforeConventionShowAnnouncement;

        @JsonProperty("system_language")
        public String systemLanguage = "en-US";
    }

    public static class SepaConfig {
        @JsonProperty("account_owner")
        public String accountOwner;

        @JsonProperty("bank_name")
        public String bankName;

        @JsonProperty("bank_address")
        public String bankAddress;

        @JsonProperty("iban")
        public String iban;

        @JsonProperty("bic")
        public String bic;

        @JsonProperty("subject_prefix")
        public String subjectPrefix;

        @JsonProperty("success_redirect")
        public String successRedirect;

        @JsonProperty("failure_redirect")
        public String failureRedirect;
    }

    public static class GroupsConfig {
        @JsonProperty("enable")
        public boolean enable;

        @JsonProperty("max_size")
        public int maxSize;

        @JsonProperty("flags")
        public List<String> flags = new ArrayList<>();
    }

    public static class RoomsConfig {
        @JsonProperty("enable")
        public boolean enable;

        @JsonProperty("flags")
        public List<String> flags = new ArrayList<>();
    }

    @JsonProperty("choices")
    public Choices choices;

    @JsonProperty("tshirtsizes")
    public InternationalizedOptions tShirtSizes;

    @JsonProperty("spoken_languages")
    public InternationalizedOptions spokenLanguages;

    @JsonProperty("registration_languages")
    public InternationalizedOptions registrationLanguages;

    @JsonProperty("countries")
    public InternationalizedOptions countries;

    @JsonProperty("downstream")
    public DownstreamConfig downstream;

    @JsonProperty("testing")
    public TestingConfig testing;

    @JsonProperty("web")
    public WebConfig web;

    @JsonProperty("sepa")
    public SepaConfig sepa;

    @JsonProperty("groups")
    public GroupsConfig groups;

    @JsonProperty("rooms")
    public RoomsConfig rooms;
}
