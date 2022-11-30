package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.web.forms.ListForm;
import org.eurofurence.regsys.web.forms.SearchForm;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the search result page
 *
 * - VIEW or ADMIN allows you to see/use this page
 * - VIEW allows using some export buttons
 * - ADMIN allows all functionality
 *
 * This page understands these parameters:
 *   again       - re-run the previous search instead of expecting new parameters
 *   direct_edit - admin pressed the Direct Edit button
 *
 * The last search is kept in the session so it can be reused,
 * ListPage stores these settings when executing a search.
 */
public class ListPage extends Page {
    // ------------ constants -----------------------

    // constants for the parameter names
    public static final String AGAIN        = "again";

    // ------------ constructors/initializers -----------------------

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createSearchForm();
        createListForm();
    }

    @Override
    protected String getPageTitle() {
        return Strings.listPage.pageTitle;
    }

    // ------------ visibility permission checks -----------------------

    public boolean mayView() {
        return     hasPermission(Constants.Permission.ADMIN)
                || hasPermission(Constants.Permission.VIEW);
    }

    // ------------ business methods -----------------------

    boolean showResult;

    public boolean showResultListing() {
        return showResult;
    }

    @Override
    public String getPageTemplateFile() {
        return "list.vm";
    }

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!mayView())
            return redirect("page/start");

        showResult = false;

        if (getRequest().getParameter(AGAIN) == null) {
            searchForm.getParameterParser().parse(getRequest());
            searchForm.storeCriteriaInSession(getRequest().getSession());
        } else {
            searchForm.getCriteriaFromSession(getRequest().getSession());
        }

        if (!hasErrors()) {
            // - load the first entry from DB
            // - check for empty set or other DB errors
            if (searchForm.find()) {
                // Okay, at least one has been found. Show the list
                showResult = true;

                // did the admin press the direct edit button? if so, jump to edit page with first attendee found
                if (!hasErrors() && getRequest().getParameter(SearchForm.SUBMIT_DIRECT_EDIT) != null) {
                    searchForm.loadNext();
                    Map<String, String> params = new HashMap<>();
                    params.put(InputPage.ATTENDEE_ID, Long.toString(searchForm.getAttendee().id));
                    return forward("page/input", params);
                }

                listForm.getParameterParser().parse(getRequest());
            }
        }

        // ---------- ---------- ---------- ---------- ---------- ---------- ---------- ---------- ----------

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        // the search form is not used directly in the page, just used to parse parameters and control the attendee search
        veloContext.put("form", getListForm().getVelocityRepresentation());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    // ------- search form ------------------

    private SearchForm searchForm;

    /**
     * create and initialize the search form object
     */
    public void createSearchForm() {
        searchForm = new SearchForm(); // also initializes
        searchForm.givePage(this); // it will get the auth from here
    }

    public SearchForm getSearchForm() {
        return searchForm;
    }

    // ------- list form ------------------

    private ListForm listForm;

    /**
     * create and initialize the list form object
     */
    public void createListForm() {
        listForm = new ListForm(); // also initializes
        listForm.givePage(this); // it will get the auth from here
    }

    public ListForm getListForm() {
        return listForm;
    }
}
