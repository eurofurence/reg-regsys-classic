package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Constants;
import org.eurofurence.regsys.backend.Strings;
import org.eurofurence.regsys.repositories.attendees.AttendeeSearchResultList;
import org.eurofurence.regsys.web.forms.Form;
import org.eurofurence.regsys.web.forms.SearchForm;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsPage extends Page {
    private List<Map<String,String>> results = new ArrayList<>();

    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        if (!hasPermission(Constants.Permission.ADMIN)) {
            return forward("page/start");
        }

        fillResults();

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);
        veloContext.put("results", results);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.commentsPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "comments.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);

        createSearchForm();
    }

    // ------- business logic --------------

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    private boolean eitherNonEmpty(String userComment, String adminComment) {
        if (!"".equals(nvl(userComment))) {
            return true;
        }
        if (!"".equals(nvl(adminComment))) {
            return true;
        }
        return false;
    }

    private void fillResults() {
        if (searchForm.find()) {
            while (searchForm.loadNext()) {
                AttendeeSearchResultList.AttendeeSearchResult att = searchForm.getAttendee();
                if (eitherNonEmpty(att.userComments, att.adminComments)) {
                    Map<String,String> entry = new HashMap<>();
                    entry.put("nickname", Form.escape(nvl(att.nickname)));
                    entry.put("userComments", Form.escape(nvl(att.userComments)));
                    entry.put("adminComments", Form.escape(nvl(att.adminComments)));
                    entry.put("id", Long.toString(att.id));
                    results.add(entry);
                }
            }
        }
    }

    // ------- search form (provides search/find logic) ------------------

    private SearchForm searchForm;

    public void createSearchForm() {
        searchForm = new SearchForm();
        searchForm.givePage(this);
    }
}
