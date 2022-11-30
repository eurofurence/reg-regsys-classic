package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 *  Represents any non-existent page in the registration system.
 */
public class NotFoundPage extends Page {
    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.startPage.notFoundTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "notfound.vm";
    }
}
