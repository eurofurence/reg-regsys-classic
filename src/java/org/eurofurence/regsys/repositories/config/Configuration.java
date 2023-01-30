package org.eurofurence.regsys.repositories.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Configuration {
    public static class ChoiceConfig {
        @JsonProperty("description")
        public String description;

        @JsonProperty("help_url")
        public String helpUrl;

        @JsonProperty("price_early")
        public int priceEarly;

        @JsonProperty("price_late")
        public int priceLate;

        @JsonProperty("price_atcon")
        public int priceAtCon;

        @JsonProperty("vat_percent")
        public float vatPercent;

        @JsonProperty("default")
        public boolean defaultValue;

        @JsonProperty("admin_only")
        public boolean adminOnly;

        @JsonProperty("read_only")
        public boolean readOnly;

        @JsonProperty("constraint")
        public String constraint;

        @JsonProperty("constraint_msg")
        public String constraintMsg;
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

        @JsonProperty("id_token_cookie_name")
        public String idTokenCookieName;

        @JsonProperty("access_token_cookie_name")
        public String accessTokenCookieName;

        @JsonProperty("api_token")
        public String apiToken; // needed for the interfaces

        @JsonProperty("nosecounter_token")
        public String nosecounterToken; // needed for the interfaces

        @JsonProperty("dd_token")
        public String ddToken;

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
    }

    @JsonProperty("choices")
    public Choices choices;

    @JsonProperty("tshirtsizes")
    public List<String> tShirtSizes;

    @JsonProperty("spoken_languages")
    public List<String> spokenLanguages;

    @JsonProperty("registration_languages")
    public List<String> registrationLanguages;

    @JsonProperty("countries")
    public List<String> countries;

    @JsonProperty("downstream")
    public DownstreamConfig downstream;

    @JsonProperty("testing")
    public TestingConfig testing;

    @JsonProperty("web")
    public WebConfig web;
}
