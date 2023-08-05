package org.eurofurence.regsys.web.forms;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.servlet.http.HttpServletRequest;

import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.CriteriaUtils;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;

/**
 *  Represents a form used to execute bulk requests on many attendees. Used by:
 *   - mass accept
 *   - bulk mail
 *  Essentially a simple text box, but it lets the user see which attendees are being worked on.
 *
 *  Parameters understood by this form come from the attendee list table
 *      allexcept=1              : invert selection
 *      {id}=1                   : this attendee was selected (de-selected if inverted)
 *      all=,{id},{id},...,{id}, : the full list of search result, to be used when inverting
 */
public abstract class AttendeeSelectionForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String ALL           = "all";
    public static final String ALLEXCEPT     = "allexcept";

    // ------------ attributes -----------------------

    protected AttendeeSearchResultList attendeeResult = new AttendeeSearchResultList();

    // ------------ constructors and initialization -----------------------

    // ---------- proxy methods for entity access -------

    // --------- Business methods ----------------------

    protected abstract String formatListEntry(AttendeeSearchResultList.AttendeeSearchResult attendee);

    private String listingBox = "Internal error - you forgot to call buildListingBox()";

    public void find() {
        try {
            AttendeeSearchCriteria criteria = CriteriaUtils.constructCriteriaByIdSet(idSet);
            criteria.fillFields = Arrays.asList("balances", "nickname", "email", "status", "registration_language");
            attendeeResult = getPage().getAttendeeService().performFindAttendees(criteria, getPage().getTokenFromRequest(), getPage().getRequestId());
        } catch (DownstreamWebErrorException e) {
            resetErrors(Strings.searchForm.nobodyFound);
            addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            resetErrors(e.getMessage());
            getPage().addException(e);
        }
    }

    public void buildListingBox() {
        StringBuilder result = new StringBuilder();
        result.append("<textarea wrap=\"off\" readonly rows=\"15\" cols=\"80\">");
        int count = 0;
        for (AttendeeSearchResultList.AttendeeSearchResult attendee: attendeeResult.attendees) {
            count++;
            result.append(escape(
                    count + " - " + formatListEntry(attendee)
                    ));
            result.append("\n");
        }
        result.append("</textarea>");
        listingBox = result.toString();
    }

    // --------------------- parameter parsers --------------------------------------------------

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    SortedSet<Long> idSet = new TreeSet<>();

    public void parseParams(HttpServletRequest request) {
        boolean allexcept = "1".equals(request.getParameter(ALLEXCEPT));
        String all = nvl(request.getParameter(ALL));
        for (String s: all.split(",")) {
            if ("".equals(s))
                continue;
            try {
                Long id = Long.parseLong(s); // throws if not an integer, silently ignored
                boolean include = "1".equals(request.getParameter(s));
                if (allexcept)
                    include = !include;
                if (include)
                    idSet.add(id);
            } catch (NumberFormatException ignore) {
            }
        }
    }

    // --------------------- form permission methods ------------------------------------------------

    // --------------------- form output methods ------------------------------------------------

    public String getListingBox() {
        return listingBox;
    }
}
