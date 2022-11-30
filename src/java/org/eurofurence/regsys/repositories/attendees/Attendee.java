package org.eurofurence.regsys.repositories.attendees;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attendee {
    @JsonProperty("id")
    public Long id;

    @JsonProperty("nickname")
    public String nickname;

    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("street")
    public String street;

    @JsonProperty("zip")
    public String zip;

    @JsonProperty("city")
    public String city;

    @JsonProperty("country")
    public String country;

    @JsonProperty("country_badge")
    public String countryBadge;

    @JsonProperty("state")
    public String state;

    @JsonProperty("email")
    public String email;

    @JsonProperty("phone")
    public String phone;

    @JsonProperty("telegram")
    public String telegram;

    @JsonProperty("partner")
    public String partner;

    @JsonProperty("birthday")
    public String birthday;

    @JsonProperty("gender")
    public String gender;

    @JsonProperty("pronouns")
    public String pronouns;

    @JsonProperty("tshirt_size")
    public String tshirtSize;

    @JsonProperty("flags")
    public String flags;

    @JsonProperty("options")
    public String options;

    @JsonProperty("packages")
    public String packages;

    @JsonProperty("user_comments")
    public String userComments;
}
