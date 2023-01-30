package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.auth.RequestAuth;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.ForbiddenException;
import org.eurofurence.regsys.repositories.errors.UnauthorizedException;
import org.eurofurence.regsys.repositories.mails.MailService;
import org.eurofurence.regsys.repositories.mails.Template;
import org.eurofurence.regsys.repositories.mails.TemplateList;

import java.util.ArrayList;
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

    public void processAccept() {
        for (Long id: idSet) {
            try {
                AttendeeService attendeeService = getPage().getAttendeeService();
                RequestAuth auth = getPage().getTokenFromRequest();
                String requestId = getPage().getRequestId();

                Attendee attendee = attendeeService.performGetAttendee(id, auth, requestId);
                String statusStr = attendeeService.performGetCurrentStatus(id, auth, requestId);
                Constants.MemberStatus status = Constants.MemberStatus.byNewRegsysValue(statusStr);

                // TODO fill variables and actually send mail, using the spoken language, or the default if not available

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

    public String fieldCidSelector() {
        List<String> values = new ArrayList<>();
        List<String> displayValues = new ArrayList<>();
        Set<String> alreadySeenCid = new HashSet<>();
        values.add("(null)");
        displayValues.add("PLEASE SELECT DESIRED ANNOUNCEMENT");
        Set<String> selectedValues = new HashSet<>();
        selectedValues.add("(null)");
        if (list.templates != null) {
            for (Template t : list.templates) {
                if (t.cid != null && t.subject != null) {
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

    // ---- legacy code ----

    /*

    public synchronized String sendBulkmail() {
        DbResultSet res = null;
        String query = "";
        int the_id = 0;
        int the_status = 0;
        int count = 0;
        String the_nick = "";
        String the_email = "";

        StringBuffer notification = new StringBuffer();

        //
        // prepare the templates
        //

        org.eurofurence.regsys.backend.Template bulkMessage = new org.eurofurence.regsys.backend.Template();

        bulkMessage.setTemplateFromString(announcement.getBody());

        //
        // send the emails
        //

        if (testAnnouncement()) {
            try {
                query = "SELECT id, nick, status, email FROM " + Config.MEMBERS_TABLE_NAME + " ORDER BY id";

                db.connect();
                res = db.executeQuery(query, ps -> {});
                while (res.get().next()) {
                    the_id = res.get().getInt("id");
                    the_status = res.get().getInt("status");
                    // Default: send to all noncancelled, nonwaiting, non-new members
                    if (((bulkIdListing == null) && (the_status < 8) && (the_status > 0))
                            || ((bulkIdListing != null) && (bulkIdListing.containsKey(Integer.toString(the_id))))) {
                        the_nick = res.get().getString("nick");
                        the_email = res.get().getString("email");
                        count++;

                        if (DEBUG) System.out.println("Processing member " + the_nick);

                        member.getFromDB(the_id);
                        String dueDate = member.getDueDate();
                        // general registration info
                        bulkMessage.reset();
                        bulkMessage.assign("%ID%", Integer.toString(member.getId()));
                        bulkMessage.assign("%NICK%", member.getNick());
                        bulkMessage.assign("%TYPE%", member.getType());
                        bulkMessage.assign("%DUES%", SessionBeanHelper.toDecimals(member.getAmountDue()) + " EUR");
                        bulkMessage.assign("%REMAINING%", SessionBeanHelper
                                .toDecimals(member.getRemainingDues() > 0.00f ? member.getRemainingDues() : 0.00f)
                                + " EUR");
                        bulkMessage.assign("%DUEDATE%", dueDate);

                        // send an email to that user, then reset updated flag.
                        email.subject = MailEncoding.encodeForEmail(announcement.getSubject());
                        email.message = MailEncoding.encodeForEmail(bulkMessage.parse());
                        email.sender = bulkSender;
                        email.from = bulkSender;

                        email.sendMail(member.getEmail());

                        notification.append(WebFormBean.escape(Integer.toString(count) + ". " + the_nick + " ("
                                + Integer.toString(the_id) + ") <" + the_email + ">")
                                + "\n");

                    }
                }
                db.disconnect();
            } catch (java.sql.SQLException e) {
                resetErrors("SQL: " + e.getMessage());
            } catch (DbException e) {
                resetErrors(e.getMessage());
            }
        } else {
            return ""; // empty string denotes that no emails have been sent
        }

        return notification.toString();
    }

    public synchronized String sendBulkmailTest() {
        Hashtable<String, String> keep = bulkIdListing;
        try {
            bulkIdListing = new Hashtable<String, String>();
            bulkIdListing.put(Integer.toString(auth.getMemberId()), "1");

            return sendBulkmail();
        } finally {
            bulkIdListing = keep;
        }
    }
    */
}
