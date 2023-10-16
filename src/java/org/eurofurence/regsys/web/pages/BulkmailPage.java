package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.web.forms.BulkmailForm;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;

/**
 *  Send bulkmail to a list of attendees as taken from any search result listing.
 *
 *  ADMIN only.
 *
 *  This page understands these request parameters:
 *      execute=(any nonempty text)
 *
 *  This page uses these forms:
 *      NavbarForm
 *      BulkmailForm
 */
public class BulkmailPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String EXECUTE   = "execute"; // != null, "" means actually run

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createBulkmailForm();
    }

    @Override
    protected String getPageTitle() {
        return Strings.bulkmailPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "bulkmail.vm";
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

        getBulkmailForm().loadTemplates();

        getBulkmailForm().parseParams(getRequest());
        getBulkmailForm().find();
        getBulkmailForm().buildListingBox();

        if (doExecute && !hasErrors()) {
            if (getBulkmailForm().processAccept()) {
                showSubmit = false;
            }
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<String, Object>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getBulkmailForm());
        veloContext.put("page", this);
        veloContext.put("showSubmit", showSubmit);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    // ------- attendee selection form ------------------

    private BulkmailForm bulkmailForm;

    public void createBulkmailForm() {
        bulkmailForm = new BulkmailForm(); // also initializes
        bulkmailForm.givePage(this); // it will get the auth from here
    }

    public BulkmailForm getBulkmailForm() {
        return bulkmailForm;
    }
}
