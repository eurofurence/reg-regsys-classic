package org.eurofurence.regsys.web.pages;

import org.eurofurence.regsys.backend.Strings;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

public class StartPage extends Page {
    @Override
    public String handleRequest() throws ServletException {
        refreshSessionTimeout();

        HashMap<String, Object> veloContext = new HashMap<>();

        veloContext.put("navbar", getNavbarForm());
        veloContext.put("page", this);

        return Page.renderTemplate(getServletContext(), "html.vm", veloContext);
    }

    @Override
    protected String getPageTitle() {
        return Strings.startPage.pageTitle;
    }

    @Override
    public String getPageTemplateFile() {
        return "start.vm";
    }

    @Override
    public void initialize(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        super.initialize(servletContext, request, response, session);
    }
}
