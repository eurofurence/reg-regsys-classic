package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.ForbiddenException;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;

/**
 *  Represents a form used to resend status emails to attendees
 *
 *  Parameters understood by this form come from the attendee list table
 *  (handled by abstract superclass)
 *      allexcept=1              : invert selection
 *      {id}=1                   : this attendee was selected (de-selected if inverted)
 *      all=,{id},{id},...,{id}, : the full list of search result, to be used when inverting
 */
public class ResendStatusEmailForm extends AttendeeSelectionForm {

    // --------- Additional Parameter Constants --------

    // --------- Business methods ----------------------

    @Override
    protected String formatListEntry(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        return attendee.nickname
             + " (" + attendee.id + ")"
             + " <" + attendee.email + ">";
    }

    protected int successCount = 0;

    public void processAccept() {
        for (Long id: idSet) {
            try {
                AttendeeService attendeeService = getPage().getAttendeeService();
                String token = getPage().getTokenFromRequest();
                String requestId = getPage().getRequestId();

                Attendee attendee = attendeeService.performGetAttendee(id, token, requestId);
                String statusStr = attendeeService.performGetCurrentStatus(id, token, requestId);
                Constants.MemberStatus status = Constants.MemberStatus.byNewRegsysValue(statusStr);

                // TODO actually call endpoint on attendee service once it's implemented

                successCount++;
            } catch (UnauthorizedException | ForbiddenException e) {
                // huh? we should be admin - maybe token expired during operation
                addError(String.format(Strings.emailPage.permAccept, id.toString()));
            } catch (DownstreamException e) {
                addError(String.format(Strings.emailPage.dbErrorAccept, id.toString()) + e.getMessage());
            }
        }
    }

    // --------------------- parameter parsers --------------------------------------------------

    // --------------------- form permission methods ------------------------------------------------

    // --------------------- form output methods ------------------------------------------------

    public String getFormHeader() {
        StringBuilder all = new StringBuilder(","); // use the filtered ids
        for (Long id: idSet) {
            all.append(id.toString()).append(",");
        }

        String result = "";

        // we no longer have the individual choices, so just pass the idSet as ALL and set ALLEXCEPT.
        result += "<FORM id=\"emailform\" ACTION=\"email\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n";
        result += "    " + Form.hiddenField(ALL, all.toString()) + "\n";
        result += "    " + Form.hiddenField(ALLEXCEPT, "1") + "\n";

        return result;
    }

    public String getStatusText() {
        String message = "";
        if (successCount > 0) {
            message += String.format(Strings.emailPage.notifySent, successCount, idSet.size());
        } else {
            if (!hasErrors())
                message += String.format(Strings.emailPage.notifyWaiting, idSet.size());
        }

        if (hasErrors())
            message += Strings.emailPage.errorsMsg;

        return message;
    }

    public String getSubmitButton(String caption, String style) {
        if (idSet.size() > successCount)
            return "<INPUT TYPE=\"SUBMIT\" NAME=\"execute\" VALUE=\"" + escape(caption) + "\" CLASS=\"" + style + "\"/>";
        return "";
    }

    public String getFormFooter() {
        return "</FORM>";
    }
}
