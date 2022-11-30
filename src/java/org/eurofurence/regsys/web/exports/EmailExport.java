package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;

public class EmailExport extends AbstractCsvExport {
    @Override
    public String getHeader() {
        return "Email-Address";
    }

    @Override
    public String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        return attendee.email;
    }
}
