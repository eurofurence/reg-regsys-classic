package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.web.forms.ResendStatusEmailForm;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;

/**
 *  Resend current status email to a list of attendees as taken from any search result listing.
 *
 *  ADMIN only.
 *
 *  This page understands these request parameters:
 *      execute=(any nonempty text)
 *
 *  This page uses these forms:
 *      NavbarForm
 *      ResendStatusEmailForm
 */
public class ResendStatusEmailPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String EXECUTE   = "execute"; // != null, "" means actually run

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createEmailForm();
    }

    @Override
    protected String getPageTitle() {
        return Strings.emailPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "resendStatusEmail.vm";
    }

    // ------------ visibility permission checks -----------------------

    // ------------ business methods -----------------------

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN)) {
            return forward("page/start");
        }

        String exec = getRequest().getParameter(EXECUTE);
        boolean doExecute = exec != null && !"".equals(exec);
        boolean showSubmit = true;

        getEmailForm().parseParams(getRequest());
        getEmailForm().find();
        getEmailForm().buildListingBox();

        if (doExecute && !hasErrors()) {
            getEmailForm().processAccept();
            showSubmit = false;
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<String, Object>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getEmailForm());
        veloContext.put("page", this);
        veloContext.put("showSubmit", showSubmit);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    // ------- attendee selection form ------------------

    private ResendStatusEmailForm emailForm;

    public void createEmailForm() {
        emailForm = new ResendStatusEmailForm(); // also initializes
        emailForm.givePage(this); // it will get the auth from here
    }

    public ResendStatusEmailForm getEmailForm() {
        return emailForm;
    }
}
