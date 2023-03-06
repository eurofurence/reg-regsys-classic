package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchCriteria;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.ForbiddenException;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;
import org.eurofurence.regsys.repositories.mails.MailSendRequest;
import org.eurofurence.regsys.repositories.mails.MailService;
import org.eurofurence.regsys.repositories.mails.Template;
import org.eurofurence.regsys.repositories.mails.TemplateList;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Represents a form used to mass mail attendees
 *
 *  Parameters understood by this form come from the attendee list table
 *  (handled by abstract superclass)
 *      allexcept=1              : invert selection
 *      {id}=1                   : this attendee was selected (de-selected if inverted)
 *      all=,{id},{id},...,{id}, : the full list of search result, to be used when inverting
 */
public class BulkmailForm extends AttendeeSelectionForm {

    // --------- Additional Parameter Constants --------

    public static final String PARAM_CID = "cid";

    // --------- Business methods ----------------------

    MailService mailService = new MailService();
    TemplateList list = new TemplateList();

    public void loadTemplates() {
        try {
            list = mailService.performFindTemplates(null, null, getPage().getTokenFromRequest(), getPage().getRequestId());
        } catch (Exception e) {
            addError(Strings.bulkmailPage.errorLoadList + e.getMessage());
            list = new TemplateList();
        }
    }

    @Override
    protected String formatListEntry(AttendeeSearchResultList.AttendeeSearchResult attendee) {
        return attendee.nickname
             + " (" + attendee.id + ")"
             + " <" + attendee.email + ">";
    }

    protected int successCount = 0;

    protected String cid = "";

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    @Override
    public void parseParams(HttpServletRequest request) {
        super.parseParams(request);
        cid = nvl(request.getParameter(PARAM_CID));
    }

    public boolean processAccept() {
        RequestAuth auth = getPage().getTokenFromRequest();
        String requestId = getPage().getRequestId();

        // verify search query ran and got results
        if (attendeeResult == null || attendeeResult.attendees == null || attendeeResult.attendees.size() == 0) {
            addError(Strings.bulkmailPage.nullError);
            return false;
        }

        // verify we have a valid cid
        if (cid == null || "".equals(cid) || "(null)".equals(cid)) {
            addError(Strings.bulkmailPage.mustSelectTemplate);
            return false;
        }

        String regsysUrl = getPage().getConfiguration().web.regsysPublicUrl;

        for (AttendeeSearchResultList.AttendeeSearchResult att: attendeeResult.attendees) {
            try {
                MailSendRequest sendRequest = new MailSendRequest();
                sendRequest.cid = cid;
                sendRequest.to.add(att.email);
                sendRequest.lang = att.registrationLanguage;
                if (!"en-US".equals(sendRequest.lang) && !"de-DE".equals(sendRequest.lang)) {
                    sendRequest.lang = "en-US";
                }
                sendRequest.variables.put("badge_number", Long.toString(att.id));
                sendRequest.variables.put("badge_number_with_checksum", att.badgeId);
                sendRequest.variables.put("nickname", att.nickname);
                sendRequest.variables.put("email", att.email);
                sendRequest.variables.put("remaining_dues", FormHelper.toCurrencyDecimals(att.currentDues));
                sendRequest.variables.put("total_dues", FormHelper.toCurrencyDecimals(att.totalDues));
                // TODO - don't have open payments balance in search response
                sendRequest.variables.put("pending_payments", "TODO not available");
                sendRequest.variables.put("due_date", att.dueDate);
                sendRequest.variables.put("regsys_url", regsysUrl);

                // variable in use by the payment failure notification system - make it possible to recognize template saves
                sendRequest.variables.put("operation", "mail template save");

                mailService.performSendMail(sendRequest, auth, requestId);

                successCount++;
            } catch (UnauthorizedException | ForbiddenException e) {
                // huh? we should be admin - maybe token expired during operation
                addError(String.format(Strings.bulkmailPage.permMail, Long.toString(att.id)));
            } catch (DownstreamException e) {
                addError(String.format(Strings.bulkmailPage.sendError, Long.toString(att.id)) + e.getMessage());
                getPage().addException(e);
            }
        }

        return true;
    }

    // --------------------- parameter parsers --------------------------------------------------

    // --------------------- form permission methods ------------------------------------------------

    // --------------------- form output methods ------------------------------------------------

    private boolean isStandardTemplate(String cid) {
        return "guest".equals(cid) || cid.startsWith("change-status-") || "payment-cncrd-adapter-error".equals(cid);
    }

    public String fieldCidSelector() {
        List<String> values = new ArrayList<>();
        List<String> displayValues = new ArrayList<>();
        Set<String> alreadySeenCid = new HashSet<>();
        values.add("(null)");
        displayValues.add("PLEASE SELECT DESIRED ANNOUNCEMENT");
        Set<String> selectedValues = new HashSet<>();
        selectedValues.add("(null)");
        if (list.templates != null) {
            // add English first if available, but filter out the standard status mails
            for (Template t : list.templates) {
                if ("en-US".equals(t.lang)) {
                    if (t.cid != null && t.subject != null && !isStandardTemplate(t.cid)) {
                        if (!alreadySeenCid.contains(t.cid)) {
                            values.add(t.cid);
                            displayValues.add(t.cid + " (" + t.subject + ")");
                            alreadySeenCid.add(t.cid);
                        }
                    }
                }
            }
            // fall back on other languages
            for (Template t : list.templates) {
                if (t.cid != null && t.subject != null && !isStandardTemplate(t.cid)) {
                    if (!alreadySeenCid.contains(t.cid)) {
                        values.add(t.cid);
                        displayValues.add(t.cid + " (" + t.subject + ")");
                        alreadySeenCid.add(t.cid);
                    }
                }
            }
        }
        return selector(true, PARAM_CID, values, displayValues, selectedValues, 1, false );
    }

    public String getFormHeader() {
        StringBuilder all = new StringBuilder(","); // use the filtered ids
        for (Long id: idSet) {
            all.append(id.toString()).append(",");
        }

        String result = "";

        // we no longer have the individual choices, so just pass the idSet as ALL and set ALLEXCEPT.
        result += "<FORM id=\"bulkmailform\" ACTION=\"bulkmail\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n";
        result += "    " + Form.hiddenField(ALL, all.toString()) + "\n";
        result += "    " + Form.hiddenField(ALLEXCEPT, "1") + "\n";

        return result;
    }

    public String getStatusText() {
        String message = "";
        if (successCount > 0) {
            message += String.format(Strings.bulkmailPage.notifyEmailsSent, successCount, idSet.size());
        } else {
            if (!hasErrors())
                message += String.format(Strings.bulkmailPage.notifyReadyToSendFormat, idSet.size());
        }

        if (hasErrors())
            message += Strings.bulkmailPage.errorsMsg;

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
