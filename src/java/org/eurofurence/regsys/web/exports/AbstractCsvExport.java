package org.eurofurence.regsys.web.exports;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base class for Csv exports
 */
public abstract class AbstractCsvExport {
    public abstract String getHeader();

    public abstract String getRecord(AttendeeSearchResultList.AttendeeSearchResult attendee);

    public Constants.ExportEncoding getEncoding() {
        return Constants.ExportEncoding.UTF8;
    }

    protected String csv(String s) {
        return "\"" + s + "\"" + ";";
    }

    protected String csv(boolean b) {
        return (b ? "X" : "") + ";";
    }

    protected String csv(int i) {
        return Integer.toString(i) + ";";
    }

    protected String csv(Long i) {
        if (i == null) {
            return ";";
        }
        return Long.toString(i) + ";";
    }

    protected Set<String> setFrom(String[] array) {
        return new HashSet<>(Arrays.asList(array));
    }

    protected String nvl(String v) {
        return v == null ? "" : v;
    }

}
