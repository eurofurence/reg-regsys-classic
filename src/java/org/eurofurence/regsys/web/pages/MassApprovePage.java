package org.eurofurence.regsys.web.pages;

import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.web.forms.MassApproveForm;

/**
 *  Bulk-Accept a list of attendees as taken from any search result listing.
 *
 *  ADMIN only.
 *
 *  This page understands these request parameters:
 *      execute=(any nonempty text)
 *
 *  This page uses these forms:
 *      NavbarForm
 *      AttendeeSelectionForm
 */
public class MassApprovePage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String EXECUTE   = "execute"; // != null, "" means actually run

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createAcceptForm();
    }

    @Override
    protected String getPageTitle() {
        return Strings.massApprovePage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "mass_approve.vm";
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

        getAcceptForm().parseParams(getRequest());
        getAcceptForm().find();
        getAcceptForm().buildListingBox();

        if (doExecute && !hasErrors()) {
            getAcceptForm().processAccept();
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<String, Object>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getAcceptForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    // ------- attendee selection form ------------------

    private MassApproveForm acceptForm;

    public void createAcceptForm() {
        acceptForm = new MassApproveForm(); // also initializes
        acceptForm.givePage(this); // it will get the auth from here
    }

    public MassApproveForm getAcceptForm() {
        return acceptForm;
    }
}
