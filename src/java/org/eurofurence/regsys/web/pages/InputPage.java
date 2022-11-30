package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.Attendee;
import org.eurofurence.regsys.web.forms.InputForm;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Represents the new/edit registrations page in the registration system.
 *
 *  - The public may enter a new registration (will not see admin fields)
 *  - Regular attendees may view and edit their own registration (will not see admin fields)
 *  - VIEW allows view of any registration, but only changes to their own registration (will not see admin fields)
 *  - ADMIN can see and edit any registration, including admin fields
 *
 *  This page understands these request parameters:
 *      param_id             id of the attendee
 *      submit_marker        "yes" means submit
 *      email_action         confirmDirty (ADMIN only): directly confirm an unconfirmed or changed email address
 *
 *  (plus any parameters introduced by forms)
 *
 *  This page uses these forms:
 *      NavbarForm
 *      InputForm
 */
public class InputPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String ATTENDEE_ID   = "param_id";

    public static final String SUBMIT_MARKER = "submit_marker"; // = "yes" means submit

    // ------------ attributes -----------------------

    private long the_id = 0;
    private boolean didSaveChanges  = false;

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createInputForm();
    }

    @Override
    protected String getPageTitle() {
        if (inputForm.isNew())
            return Strings.inputPage.pageTitleNew;
        return Strings.inputPage.pageTitleEdit;
    }

    // ------------ visibility permission checks -----------------------

    // when these are called, global check for registration enabled is already done
    public boolean mayView() {
        return     hasPermission(Constants.Permission.ADMIN)
                || hasPermission(Constants.Permission.VIEW)
                || (!readonlyExceptAdmin() && the_id == 0)
                || (isMyBadgeNumber(the_id));
    }

    public boolean mayEdit() {
        return     hasPermission(Constants.Permission.ADMIN)
                || (!readonlyExceptAdmin() && the_id == 0)
                || (!readonlyExceptAdmin() && isMyBadgeNumber(the_id));
    }

    public boolean mayViewAdmin() {
        return     hasPermission(Constants.Permission.ADMIN)
                || hasPermission(Constants.Permission.VIEW);
    }

    public boolean mayEditAdmin() {
        return     hasPermission(Constants.Permission.ADMIN);
    }

    // ------------ business methods -----------------------

    @Override
    public String getPageTemplateFile() {
        return "input.vm";
    }

    @Override
    public ArrayList<String> getHeaderJsFileList() {
        ArrayList<String> lst = new ArrayList<>();
        lst.add("js/input_js.vm");
        return lst;
    }

    protected boolean identityAndAuthorizationLoadsAttendee() throws ServletException {
        the_id = 0;
        String param_id_string = getRequest().getParameter(ATTENDEE_ID);

        if (param_id_string == null || param_id_string.equals("")) {
            List<Long> badgeNumbers = getMyBadgeNumbers();
            if (!badgeNumbers.isEmpty()) {
                the_id = badgeNumbers.get(0);
            }
        } else {
            try {
                the_id = Integer.parseInt(param_id_string);
            } catch (Exception e) {
                // a non-integer parameter has been given.
                return false;
            }
        }

        if (the_id != 0) {
            if (mayView() && the_id > 0) {
                try {
                    inputForm.getFromDB(the_id);
                } catch (Exception e) {
                    addError(e.getMessage());
                    inputForm.initialize(); // blank totally
                }
            } else {
                // user cannot view this registration.
                return false;
            }
        }
        if (the_id == 0 && readonlyExceptAdmin())
            return false;

        return true;
    }

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!isRegistrationEnabled()) {
            return forward("page/start");
        }
        if (!isLoggedIn()) {
            return forward("page/start");
        }

        if (!identityAndAuthorizationLoadsAttendee())
            return redirect("page/start"); // They tried something forbidden.

        boolean wasNew = inputForm.isNew();
        boolean hasSubmitMarker = "yes".equals(getRequest().getParameter(SUBMIT_MARKER));
        boolean showRegWaitMailInfo = false;

        if (!readonlyExceptAdmin() || hasPermission(Constants.Permission.ADMIN)) {
            if (hasSubmitMarker) {
                // check permissions for submitting data: VIEW may view, but only ADMIN and oneself may submit
                if (!hasPermission(Constants.Permission.ADMIN) && (the_id != 0) && !isMyBadgeNumber(the_id)) {
                    Map<String, String> params = new HashMap<>();
                    params.put("error", Strings.inputPage.permCanOnlyEditYourself);
                    // TODO reset page (for showing errors and as target for forbidden stuff)
                    return forward("page/start", params);
                }

                resetErrors();

                if (hasPermission(Constants.Permission.ADMIN)) {
                    inputForm.getParameterParser().parseAdminParams(getRequest());
                }

                inputForm.getParameterParser().parseRegularParams(getRequest());

                if (!hasErrors()) {
                    if (inputForm.processAccept(wasNew)) {
                        if (wasNew) {
                            showRegWaitMailInfo = true;

                            // update logged in attendee so Navbar no longer shows New Registration, but avoid downstream request
                            Attendee savedAttendee = inputForm.getAttendee();
                            cachedMyBadgeNumbers = Collections.singletonList(savedAttendee.id);
                            cachedLoggedInAttendee = savedAttendee;
                        } else {
                            // notify user of successful changes only if he is actually changing, not registering for the first time
                            didSaveChanges = true;
                        }
                    }
                }
            }
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        if (hasPermission(Constants.Permission.ADMIN) || hasPermission(Constants.Permission.VIEW)) {
            // if an admin might just accept a yet non-participating member, set the due dates accordlingly
            if (hasPermission(Constants.Permission.ADMIN) && !inputForm.getStatus().isParticipating()) {
                // TODO
                // inputForm.tentativeSetDueDates();
            }
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getInputForm().getVelocityRepresentation());
        veloContext.put("page", this);

        veloContext.put("showRegWaitMailInfo", showRegWaitMailInfo);
        veloContext.put("didSaveChanges", didSaveChanges);
        veloContext.put("wasNew", wasNew);

        veloContext.put("termsURL", Strings.conf.termsURL);
        veloContext.put("waiverURL", Strings.conf.waiverURL);
        veloContext.put("rulesURL",  Strings.conf.rulesURL);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    public boolean hasGreenText() {
        return inputForm.greenText.size() > 0;
    }

    public List<String> getGreenText() {
        return inputForm.greenText;
    }

    // ------- input form ------------------

    private InputForm inputForm;

    public void createInputForm() {
        inputForm = new InputForm();
        inputForm.givePage(this);
        inputForm.initialize();
    }

    public InputForm getInputForm() {
        return inputForm;
    }
}
