package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;
import org.eurofurence.regsys.repositories.mails.MailService;
import org.eurofurence.regsys.repositories.mails.Template;
import org.eurofurence.regsys.web.pages.AnnouncementsPage;

import javax.servlet.http.HttpServletRequest;

public class AnnouncementForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String PARAM_CID = "cid";
    public static final String PARAM_LANG = "lang";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_BODY = "body";

    // handle parameters from form:
    // action = list (default), edit (new with template_id=0), delete, store
    // for action "edit": template_id (0=new)
    // for action "store": template_id (0=new), name, subject, body
    // for action "delete": template_id, sure (presence is a flag)

    public static final String PARAM_ID = "template_id";

    // ------------ attributes -----------------------

    private String id;

    private Template current = new Template();

    // ------------ constructors and initialization -----------------------

    private final MailService mailService = new MailService();

    public AnnouncementForm() {
    }

    public void initialize() {
        makeNew();
    }

    public void makeNew() {
        id = null;
        current = new Template();
    }

    public void makeCopy() {
        // change so it saves as a copy
        current.uuid = null;
        id = null;
        current.cid = "Copy of " + current.cid;
    }

    // ---------- proxy methods for entity access -------

    public boolean isNew() {
        return id == null;
    }

    // --------- Business methods ----------------------

    public void withErrorHandling(Runnable operation, String errMsg) {
        try {
            operation.run();
        } catch (DownstreamWebErrorException e) {
            addError(errMsg + e.getMessage());
            addWebErrors(e.getErr());
        } catch (DownstreamException e) {
            addError(e.getMessage());
        }
    }

    public void loadAnnouncement() {
        withErrorHandling(() -> {
            if (id != null) {
                current = mailService.performGetTemplate(id, getPage().getTokenFromRequest(), getPage().getRequestId());
            }
        }, String.format(Strings.announcementPage.dbErrorLoadById, id));

        // fix line terminators for the web
        current.data = current.data.replaceAll("\r", "");
    }

    public void storeAnnouncement() {
        // ensure correct line terminators for sending mail (wants crlf)
        current.data = current.data.replaceAll("\r", "").replaceAll("\n", "\r\n");

        withErrorHandling(() -> {
            if (id == null) {
                id = mailService.performCreateTemplate(current, getPage().getTokenFromRequest(), getPage().getRequestId());
            } else {
                current.uuid = id;
                mailService.performUpdateTemplate(id, current, getPage().getTokenFromRequest(), getPage().getRequestId());
            }
        }, Strings.announcementPage.dbErrorSave);

        // fix line terminators for the web
        current.data = current.data.replaceAll("\r", "");
    }

    public void deleteAnnouncement() {
        withErrorHandling(() -> {
            mailService.performDeleteTemplate(id, getPage().getTokenFromRequest(), getPage().getRequestId());
        }, Strings.announcementPage.dbErrorDelete);
    }

    public String getCid() {
        return current.cid;
    }

    public String getLang() {
        return current.lang;
    }

    // --------------------- parameter parsers --------------------------------------------------

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    /**
     * This inner class encapsules all functions that are provided by this form for parsing request parameters
     * - keeps some structure to the main class, separating this concern out from the regular business methods
     *   until they can eventually be moved into activities
     * - folds away nicely if not needed
     */
    public class ParameterParser {
        private void setId(String idStr) {
            if (idStr != null && !"".equals(idStr)) {
                id = idStr;
            }
        }

        public void parseIdParam(HttpServletRequest request) {
            setId(request.getParameter(PARAM_ID));
        }

        public void parseFormParams(HttpServletRequest request) {
            current.cid = nvl(request.getParameter(PARAM_CID));
            current.lang = nvl(request.getParameter(PARAM_LANG));
            current.data = nvl(request.getParameter(PARAM_BODY));
            current.subject = nvl(request.getParameter(PARAM_SUBJECT));
        }
    }

    public ParameterParser getParameterParser() {
        return new ParameterParser();
    }

    // --------------------- form permission methods ------------------------------------------------

    // --------------------- form output methods ------------------------------------------------

    /**
     * This inner class encapsules all functions that are provided by this form for velocity templates
     * - avoids calling any methods not listed here from Velocity
     * - folds away nicely if not needed
     */
    public class VelocityRepresentation {
        // urls

        public String deleteYesUrl() {
            return "announcements?action=delete&template_id="+ id + "&sure=yes";
        }

        // properties

        // attributes

        public String getId() {
            return Form.escape(id);
        }

        public String printCid() {
            return Form.escape(current.cid);
        }

        public String printLang() {
            return Form.escape(current.lang);
        }

        public String printSubject() {
            return Form.escape(current.subject);
        }

        public String printBody() {
            return Form.escape(current.data);
        }

        // form fields

        public String editFormHeader() {
            return "<FORM ACTION=\"announcements\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n" +
                    hiddenField("action", "store") + "\n" +
                    hiddenField(PARAM_ID, id);
        }

        public String editFormFooter() {
            return "</FORM>";
        }

        public String cidField() {
            return textField(true, PARAM_CID, current.cid, 64, 64);
        }

        public String langField() {
            return textField(true, PARAM_LANG, current.lang, 5, 5);
        }

        public String subjectField() {
            return textField(true, PARAM_SUBJECT, current.subject, 80, 160);
        }

        public String bodyField() {
            return textArea(true, PARAM_BODY, current.data, 20, 80, "mail");
        }

        public String testSendCheckbox() {
            return checkbox(true, AnnouncementsPage.PARAM_TEST, "yes", "yes");
        }

    }

    public VelocityRepresentation getVelocityRepresentation() {
        return new VelocityRepresentation();
    }
}
