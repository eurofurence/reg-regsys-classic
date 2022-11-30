package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;

import java.util.Set;

public class SponsorExport extends AbstractCsvExport {
    @Override
    public String getHeader() {
        return ""; // no header, method not used
    }

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        Set<String> flags = setFrom(attendee.flags.split(","));
        if (flags.contains("anon")) {
            return attendee.nickname;
        } else {
            return attendee.nickname + " (" + attendee.firstName + " " + attendee.lastName + ")";
        }
    }
}
