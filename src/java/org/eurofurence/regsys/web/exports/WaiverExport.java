package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.web.forms.FormHelper;

import java.util.Set;

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

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> packages = setFrom(attendee.packages.split(","));
        Set<String> flags = setFrom(attendee.flags.split(","));

        boolean isGuest = flags.contains("guest");
        boolean isDay = packages.contains("day-thu") || packages.contains("day-fri") || packages.contains("day-sat");

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
