package org.eurofurence.regsys.web.forms;

import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.AttendeeService;
import org.eurofurence.regsys.repositories.attendees.BanRule;
import org.eurofurence.regsys.repositories.errors.DownstreamException;
import org.eurofurence.regsys.repositories.errors.DownstreamWebErrorException;

import javax.servlet.http.HttpServletRequest;

public class BanForm extends Form {

    // ------------ parameter constants -----------------------

    public static final String PARAM_NAME_PATTERN = "namePattern";
    public static final String PARAM_NICK_PATTERN = "nickPattern";
    public static final String PARAM_EMAIL_PATTERN = "emailPattern";
    public static final String PARAM_REASON = "reason";

    // handle parameters from form:
    // action = list (default), edit (new with id=0), delete, store
    // for action "edit": id (0=new)
    // for action "copy": id
    // for action "store": id (0=new), namePattern, nickPattern, emailPattern, reason
    // for action "delete": id, sure (presence is a flag)

    public static final String PARAM_ID = "id";

    // ------------ attributes -----------------------

    private long id = 0;

    private BanRule current = new BanRule();

    // ------------ constructors and initialization -----------------------

    private final AttendeeService attendeeService = new AttendeeService();

    public BanForm() {
    }

    public void initialize() {
        makeNew();
    }

    public void makeNew() {
        id = 0;
        current = new BanRule();
    }

    public void makeCopy() {
        // change so it saves as a copy
        current.id = 0L;
        id = 0L;
    }

    // ---------- proxy methods for entity access -------

    public boolean isNew() {
        return id == 0;
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

    public void loadBan() {
        withErrorHandling(() -> {
            current = getPage().getAttendeeService().performGetBan(id, getPage().getTokenFromRequest(), getPage().getRequestId());
        }, String.format(Strings.bansPage.dbErrorLoadById, id));
    }

    public void storeBan() {
        withErrorHandling(() -> {
            if (id == 0) {
                id = getPage().getAttendeeService().performAddBan(current, getPage().getTokenFromRequest(), getPage().getRequestId());
            } else {
                current.id = id;
                getPage().getAttendeeService().performUpdateBan(current, getPage().getTokenFromRequest(), getPage().getRequestId());
            }
        }, Strings.bansPage.dbErrorSave);
    }

    public void deleteBan() {
        withErrorHandling(() -> {
            getPage().getAttendeeService().performDeleteBan(id, getPage().getTokenFromRequest(), getPage().getRequestId());
        }, Strings.bansPage.dbErrorDelete);
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
            try {
                id = Long.parseLong(idStr);
                if (id < 0) id = 0;
            } catch (Exception e) {
                // ignore - at worst saves a new ban if user has been meddling with our parameters
            }
        }

        public void parseIdParam(HttpServletRequest request) {
            setId(request.getParameter(PARAM_ID));
        }

        public void parseFormParams(HttpServletRequest request) {
            current.namePattern = nvl(request.getParameter(PARAM_NAME_PATTERN));
            current.nicknamePattern = nvl(request.getParameter(PARAM_NICK_PATTERN));
            current.emailPattern = nvl(request.getParameter(PARAM_EMAIL_PATTERN));
            current.reason = nvl(request.getParameter(PARAM_REASON));
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
            return "bans?action=delete&id="+ id + "&sure=yes";
        }

        // properties

        // attributes

        public String getBanId() {
            return Long.toString(id);
        }

        public String printNamePattern() {
            return Form.escape(current.namePattern);
        }

        public String printNicknamePattern() {
            return Form.escape(current.nicknamePattern);
        }

        public String printEmailPattern() {
            return Form.escape(current.emailPattern);
        }

        public String printReason() {
            return Form.escape(current.reason);
        }

        // form fields

        public String editFormHeader() {
            return "<FORM ACTION=\"bans\" METHOD=\"POST\" accept-charset=\"UTF-8\">\n" +
                    hiddenField("action", "store") + "\n" +
                    hiddenField(PARAM_ID, Long.toString(id));
        }

        public String editFormFooter() {
            return "</FORM>";
        }

        // textField(boolean editable, String name, String value, int displaySize, int maxContentLength)

        public String namePatternField() {
            return textField(true, PARAM_NAME_PATTERN, current.namePattern, 80, 250);
        }

        public String nicknamePatternField() {
            return textField(true, PARAM_NICK_PATTERN, current.nicknamePattern, 80, 250);
        }

        public String emailPatternField() {
            return textField(true, PARAM_EMAIL_PATTERN, current.emailPattern, 80, 250);
        }

        public String reasonField() {
            return textField(true, PARAM_REASON, current.reason, 80, 250);
        }

    }

    public VelocityRepresentation getVelocityRepresentation() {
        return new VelocityRepresentation();
    }
}
