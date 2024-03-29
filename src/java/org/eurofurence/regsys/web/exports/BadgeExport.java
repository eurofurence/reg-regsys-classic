package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.web.forms.FormHelper;

import java.util.Set;

public class BadgeExport extends AbstractCsvExport {
    private final Configuration config = new ConfigService(HardcodedConfig.CONFIG_URL).getConfig();

    @Override
    public String getHeader() {
        return csv("Id")
                + csv("Nick")
                + csv("First")
                + csv("Last")
                + csv("Fullname")
                + csv("Street")
                + csv("Zip")
                + csv("City")
                + csv("Email")
                + csv("Country")
                + csv("Languages")
                + csv("Status")
                + csv("Registr.")
                + csv("Total")
                + csv("Dues")
                + csv("Type")
                + csv("Sponsor")
                + csv("NotParticipating")
                + csv("Druck-Attendee")
                + csv("Druck-Sponsor")
                + csv("Druck-Guest")
                + csv("Druck-SuperSponsor")
                + csv("Druck-Staff")
                + csv("Druck-Director")
                + csv("Day");
    }

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> packages = setFrom(attendee.packages.split(","));
        Set<String> flags = setFrom(attendee.flags.split(","));

        boolean isNotParticipating = attendee.status != null && Constants.MemberStatus.byNewRegsysValue(attendee.status).isNotParticipating();
        boolean isStaff = flags.contains("staff");
        boolean isDirector = flags.contains("director");

        boolean isGuest = flags.contains("guest");
        boolean isDay = packages.stream().anyMatch(e -> e != null && e.startsWith("day-"));
        boolean isRegular = !isGuest && !isDay && !isNotParticipating;

        boolean isSupersponsor = isRegular && packages.contains("sponsor2");
        boolean isSponsor = isRegular && !isSupersponsor && packages.contains("sponsor");;
        boolean isAttendee = isRegular && !isSupersponsor && !isSponsor;

        String regType = "regular";
        if (isGuest) {
            regType = "guest";
        } else if (isDay) {
            regType = "day";
        }

        return csv(attendee.badgeId)
                + csv(attendee.nickname)
                + csv(attendee.firstName)
                + csv(attendee.lastName)
                + csv(attendee.firstName + " " + attendee.lastName)
                + csv(attendee.street)
                + csv(attendee.zip)
                + csv(attendee.city)
                + csv(attendee.email)
                + csv(lookupCountryName(nvl(attendee.country)))
                + csv(nvl(attendee.spokenLanguages))
                + csv(attendee.status)
                + csv(attendee.registered)
                + csv(FormHelper.toCurrencyDecimals(attendee.totalDues))
                + csv(FormHelper.toCurrencyDecimals(attendee.currentDues))
                + csv(regType)
                + csv(getBadgeSponsorDesc(attendee))
                + csv(isNotParticipating ? "x" : "")
                + csv(isAttendee ? "x" : "")
                + csv(isSponsor ? "x" : "")
                + csv(isGuest ? "x" : "")
                + csv(isSupersponsor ? "x" : "")
                + csv(isStaff ? "x" : "")
                + csv(isDirector ? "x" : "")
                + csv(isDay ? getBadgeDayGuestCode(attendee) : "");
    }

    private String lookupCountryName(String code) {
        if (code == null || code.equals("")) {
            return "";
        }
        return config.countries.lookup(code, "en-US");
    }

    private String getBadgeSponsorDesc(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> packages = setFrom(attendee.packages.split(","));
        if (packages.contains("sponsor")) {
            return "Sponsor";
        } else if (packages.contains("sponsor2")) {
            return "Supersponsor";
        } else {
            return "";
        }
    }

    private String getBadgeDayGuestCode(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> packages = setFrom(attendee.packages.split(","));
        String result = "";
        if (packages.contains("day-thu")) {
            result += "DO";
        }
        if (packages.contains("day-fri")) {
            if (result.length() > 0)
                result += ",";
            result += "FR";
        }
        if (packages.contains("day-sat")) {
            if (result.length() > 0)
                result += ",";
            result += "SA";
        }
        if (packages.contains("day-sun")) {
            if (result.length() > 0)
                result += ",";
            result += "SU";
        }
        if (packages.contains("day-mon")) {
            if (result.length() > 0)
                result += ",";
            result += "MO";
        }
        if (packages.contains("day-tue")) {
            if (result.length() > 0)
                result += ",";
            result += "TU";
        }
        if (packages.contains("day-wed")) {
            if (result.length() > 0)
                result += ",";
            result += "WE";
        }
        return result;
    }
}
