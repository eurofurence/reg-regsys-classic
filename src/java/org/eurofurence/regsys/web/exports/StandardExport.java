package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.backend.persistence.TypeChecks;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

public class StandardExport extends AbstractCsvExport {
    @Override
    public String getHeader() {
        return csv("Lastname") + csv("Firstname") + csv("Arrival Date") + csv("Departure Date") + csv("Room category")
                + csv("Room number") + csv("Dog?") + csv("Wheelchair?") + csv("Birthday") + csv("Hoehenangst?")
                + csv("Street") + csv("ZIP") + csv("City") + csv("State") + csv("Country") + csv("Regnr/ID")
                + csv("Nick") + csv("Email");
    }

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> packages = setFrom(attendee.packages.split(","));
        Set<String> flags = setFrom(attendee.flags.split(","));

        // calculate arrival/departure dates
        Calendar cal = new GregorianCalendar();
        cal.setTime(TypeChecks.parseStaticDate(Strings.conf.conStart));
        //if (member.getPackageByCode("early").count > 0) {
        //    cal.add(Calendar.DAY_OF_MONTH, -1);
        //}
        String arrivalDate = TypeChecks.formatDate(cal.getTime());
        cal = new GregorianCalendar();
        cal.setTime(TypeChecks.parseStaticDate(Strings.conf.conEnd));
        //if (member.getPackageByCode("late").count > 0) {
        //    cal.add(Calendar.DAY_OF_MONTH, 1);
        //}
        String departureDate = TypeChecks.formatDate(cal.getTime());

        String dog = ""; // member.getPackageByCode("doggy").count > 0 ? "X" : "";
        String earthbound = ""; // attendee.getFlag("earthbound").count > 0 ? "X" : "";
        String wheelchair = flags.contains("hc") ? "X" : "";

        String line = csv(attendee.lastName)
                + csv(attendee.firstName)
                + csv(arrivalDate)
                + csv(departureDate)
                // TODO attendee.getRoomOption().description
                + csv("MISSING")
                // TODO attendee.getRoomAssign()
                + csv("MISSING")
                + csv(dog)
                + csv(wheelchair)
                + csv(attendee.birthday)
                + csv(earthbound)
                + csv(attendee.street)
                + csv(attendee.zip)
                + csv(attendee.city)
                + csv(attendee.state)
                + csv(nvl(attendee.country))
                + csv(attendee.id)
                + csv(attendee.nickname)
                + csv(attendee.email);
        return line;
    }
}
