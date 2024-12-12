package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.PackageInfo;
import org.eurofurence.regsys.web.forms.FormHelper;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class WaiverExport extends AbstractCsvExport {
    @Override
    public String getHeader() {
        return csv("Id")
                + csv("Nick")
                + csv("First")
                + csv("Last")
                + csv("Street")
                + csv("Zip")
                + csv("City")
                + csv("Country")
                + csv("Email")
                + csv("Minor?")
                + csv("Type")
                + csv("Status")
                + csv("Registr.")
                // + csv( "Arrives" )
                // + csv( "Departs" )
                + csv("Total")
                + csv("Dues")
                + csv("Room");
    }

    private boolean packagesMatch(List<PackageInfo> packagesList, Predicate<PackageInfo> safeFilter) {
        return packagesList.stream().anyMatch(e -> e != null && e.name != null && safeFilter.test(e));
    }

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> flags = setFrom(attendee.flags.split(","));

        boolean isGuest = flags.contains("guest");
        boolean isDay = packagesMatch(attendee.packagesList, e -> e.name.startsWith("day-"));

        String regType = "regular";
        if (isGuest) {
            regType = "guest";
        } else if (isDay) {
            regType = "day";
        }

        return csv(attendee.id)
                + csv(attendee.nickname)
                + csv(attendee.firstName)
                + csv(attendee.lastName)
                + csv(attendee.street)
                + csv(attendee.zip)
                + csv(attendee.city)
                + csv(attendee.country)
                // + csv(Config.lookupCountry(attendee.getState(), attendee.getCountry()))
                + csv(attendee.email)
                // TODO attendee.isMinor() ? "MINOR" : ""
                + csv("MISSING")
                + csv(regType)
                + csv(attendee.status)
                + csv(attendee.registered)
                // + csv(attendee.getArrival())
                // + csv(attendee.getDeparture())
                + csv(FormHelper.toCurrencyDecimals(attendee.totalDues))
                + csv(FormHelper.toCurrencyDecimals(attendee.currentDues))
                // TODO attendee.getRoomAssign()
                + csv("MISSING");
    }
}
