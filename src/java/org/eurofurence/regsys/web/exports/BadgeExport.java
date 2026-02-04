package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.HardcodedConfig;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.PackageInfo;
import org.eurofurence.regsys.repositories.config.ConfigService;
import org.eurofurence.regsys.repositories.config.Configuration;
import org.eurofurence.regsys.web.forms.FormHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

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
                + csv("Day")
                + csv("Day-Mon")
                + csv("Day-Tue")
                + csv("Day-Wed")
                + csv("Day-Thu")
                + csv("Day-Fri")
                + csv("Day-Sat")
                + csv("Day-Sun")
                + csv("Ultra-Sponsor")
                + csv("Contributor");
    }

    private boolean packagesMatch(List<PackageInfo> packagesList, Predicate<PackageInfo> safeFilter) {
        return packagesList.stream().anyMatch(e -> e != null && e.name != null && safeFilter.test(e));
    }
    private boolean packagesContains(List<PackageInfo> packagesList, String packageName) {
        return packagesList.stream().anyMatch(e -> e != null && e.name != null && e.name.equals(packageName));
    }
    private long lookupCount(List<PackageInfo> packagesList, String packageName) {
        return packagesList.stream()
                .filter(e -> e != null && e.name != null && e.name.equals(packageName))
                .map(e -> e.count)
                .mapToLong(Long::longValue)
                .sum();
    }

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> flags = setFrom(attendee.flags.split(","));

        boolean isNotParticipating = attendee.status != null && Constants.MemberStatus.byNewRegsysValue(attendee.status).isNotParticipating();
        boolean isStaff = flags.contains("staff");
        boolean isDirector = flags.contains("director");

        boolean isGuest = flags.contains("guest");
        boolean isDay = packagesMatch(attendee.packagesList, e -> e.name.startsWith("day-"));
        boolean isRegular = !isGuest && !isDay && !isNotParticipating;

        boolean isSupersponsor = isRegular && packagesContains(attendee.packagesList, "sponsor2");
        boolean isSponsor = isRegular && !isSupersponsor && packagesContains(attendee.packagesList, "sponsor");;
        boolean isContributor = isRegular && !isSupersponsor && !isSponsor && packagesContains(attendee.packagesList, "contributor");
        boolean isAttendee = isRegular && !isSupersponsor && !isSponsor && !isContributor;

        String regType = "regular";
        if (isGuest) {
            regType = "guest";
        } else if (isDay) {
            regType = "day";
        }

        long ultraSponsorCount = lookupCount(attendee.packagesList, "ultrasponsor");

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
                + csv(isDay ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-mon") ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-tue") ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-wed") ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-thu") ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-fri") ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-sat") ? "x" : "")
                + csv(packagesContains(attendee.packagesList, "day-sun") ? "x" : "")
                + csv(ultraSponsorCount > 0 ? Long.toString(ultraSponsorCount) : "")
                + csv(isContributor ? "x" : "");
    }

    private String lookupCountryName(String code) {
        if (code == null || code.equals("")) {
            return "";
        }
        return config.countries.lookup(code, "en-US");
    }

    private String getBadgeSponsorDesc(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        if (packagesContains(attendee.packagesList, "sponsor")) {
            return "Sponsor";
        } else if (packagesContains(attendee.packagesList, "sponsor2")) {
            return "Supersponsor";
        } else if (packagesContains(attendee.packagesList, "contributor")) {
            return "Contributor";
        } else {
            return "";
        }
    }
}
