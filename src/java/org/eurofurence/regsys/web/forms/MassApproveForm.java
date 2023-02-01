package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.attendees.StatusChange;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.ForbiddenException;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;

/**
 *  Represents a form used to mass accept attendees
 *
 *  Parameters understood by this form come from the attendee list table
 *  (handled by abstract superclass)
 *      allexcept=1              : invert selection
 *      {id}=1                   : this attendee was selected (de-selected if inverted)
 *      all=,{id},{id},...,{id}, : the full list of search result, to be used when inverting
 */
public class MassApproveForm extends AttendeeSelectionForm {

    // --------- Business methods ----------------------

    @Override
    protected String formatListEntry(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        return attendee.firstName + " '" + attendee.nickname + "' " + attendee.lastName
             + ", " + attendee.city + ", " + attendee.country
             + " (" + attendee.id + ")"
             + " <" + attendee.email + ">";
    }

    protected int successCount = 0;

    public void processAccept() {
        for (Long id: idSet) {
            try {
                AttendeeService attendeeService = getPage().getAttendeeService();
                RequestAuth auth = getPage().getTokenFromRequest();
                String requestId = getPage().getRequestId();

                Attendee attendee = attendeeService.performGetAttendee(id, auth, requestId);
                String statusStr = attendeeService.performGetCurrentStatus(id, auth, requestId);
                Constants.MemberStatus status = Constants.MemberStatus.byNewRegsysValue(statusStr);

                if (status != Constants.MemberStatus.NEW) {
                    addError(String.format(Strings.massApproveForm.attendeeNotNew, id.toString()));
                } else {
                    StatusChange change = new StatusChange();
                    change.status = Constants.MemberStatus.APPROVED.newRegsysValue();
                    change.comment = "regsys classic bulk approve";
                    attendeeService.performStatusChange(id, change, auth, requestId);
                }
                successCount++;
            } catch (UnauthorizedException | ForbiddenException e) {
                // huh? we should be admin - maybe token expired during operation
                addError(String.format(Strings.massApproveForm.permAccept, id.toString()));
            } catch (DownstreamException e) {
                addError(String.format(Strings.massApproveForm.dbErrorAccept, id.toString()) + e.getMessage());
            }
        }
    }

    // --------------------- parameter parsers --------------------------------------------------

    // --------------------- form permission methods ------------------------------------------------

    // --------------------- form output methods ------------------------------------------------

    public String getFormHeader() {
        StringBuilder all = new StringBuilder(","); // use the filtered ids, some banned ones may have been removed
        for (Long id: idSet) {
            all.append(id.toString()).append(",");
        }

        String result = "";

        // we no longer have the individual choices, so just pass the idSet as ALL and set ALLEXCEPT.
        result += "<FORM id=\"acceptform\" ACTION=\"mass-approve\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n";
        result += "    " + Form.hiddenField(ALL, all.toString()) + "\n";
        result += "    " + Form.hiddenField(ALLEXCEPT, "1") + "\n";

        return result;
    }

    public String getStatusText() {
        String message = "";
        if (successCount > 0) {
            message += String.format(Strings.massApproveForm.successMsg, successCount, idSet.size());
        } else {
            if (!hasErrors())
                message += String.format(Strings.massApproveForm.readyMsg, idSet.size());
        }

        if (hasErrors())
            message += Strings.massApproveForm.errorsMsg;

        return message;
    }

    public String getSubmitButton(String caption, String errorCaption, String style) {
        if (hasErrors())
            return "<INPUT TYPE=\"SUBMIT\" NAME=\"reload\" VALUE=\"" + escape(errorCaption) + "\" CLASS=\"" + style + "\"/>";
        if (idSet.size() > successCount)
            return "<INPUT TYPE=\"SUBMIT\" NAME=\"execute\" VALUE=\"" + escape(caption) + "\" CLASS=\"" + style + "\"/>";
        return "";
    }

    public String getFormFooter() {
        return "</FORM>";
    }
}
