package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.web.forms.SearchForm;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * Represents the search registrations page
 *
 * - VIEW or ADMIN allows you to see/use this page
 *
 * This page understands these parameters:
 *   resetform - any value will blank the form
 *
 * The last search is kept in the session, so it can be reused,
 * ListPage stores these settings when executing a search.
 */
public class SearchPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String RESET_FORM   = "resetform";

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createSearchForm();
    }

    @Override
    protected String getPageTitle() {
        return Strings.searchPage.pageTitle;
    }

    // ------------ visibility permission checks -----------------------

    public boolean mayView() {
        return     hasPermission(Constants.Permission.ADMIN)
                || hasPermission(Constants.Permission.VIEW);
    }

    // ------------ business methods -----------------------

    @Override
    public String getPageTemplateFile() {
        return "search.vm";
    }

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!mayView())
            return redirect("page/start");

        // by default, start from the last search
        searchForm.getCriteriaFromSession(getSession());

        if (getRequest().getParameter(RESET_FORM) != null) {
            searchForm.initialize();
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("form", getSearchForm().getVelocityRepresentation());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    // ------- search form ------------------

    private SearchForm searchForm;

    public void createSearchForm() {
        searchForm = new SearchForm(); // also initializes
        searchForm.givePage(this); // it will get the auth from here
    }

    public SearchForm getSearchForm() {
        return searchForm;
    }
}
